from torchvision.transforms import Compose
from torch.nn import Module
from torch.optim import Optimizer
from torch.optim.lr_scheduler import _LRScheduler
from torchvision import datasets
import torch.onnx as onnx
import torch
import time
import copy
import os

class ModelTrainer:
    def __init__(self,model:Module,criterion:Module,optimizer:Optimizer,schedular:_LRScheduler,device:str):
        self.model = model
        self.criterion = criterion
        self.optimizer = optimizer
        self.schedular = schedular
        self.device = device
    
    def make_dataloader(self,transformars:dict[str,Compose],data_path:str,batch_size:int=16,shuffle:bool=True,num_workers:int=1):
        image_datasets = {x:datasets.ImageFolder(os.path.join(data_path,x),transformars[x]) for x in ["train","test"]}
        self.dataloaders = {x:torch.utils.data.DataLoader(image_datasets[x],batch_size=batch_size,shuffle=shuffle,num_workers=num_workers)\
                       for x in ["train","test"]}
        self.dataset_sizes = {x:len(image_datasets[x]) for x in ["train","test"]}
        self.class_names = image_datasets["train"].classes

    def get_data_loader(self) -> dict[str,Compose]:
        return self.dataloaders
    
    def get_class_names(self) -> list[str]:
        return self.class_names
    
    def train_model(self,num_epochs) -> Module:
        since = time.time()
        
        best_model_wts = copy.deepcopy(self.model.state_dict())
        best_acc = 0.0

        for epoch in range(num_epochs): #エポックごとの処理
            print(f"Epoch {epoch}/{num_epochs-1}")
            print("-"*10)

            for phase in ["train","test"]:
                if phase == "train":
                    self.model.train()
                else:
                    self.model.eval()
            
                running_loss = 0.0
                running_corrects = 0

                for inputs, labels in self.dataloaders[phase]: #バッチごとの処理
                    inputs = inputs.to(self.device)
                    labels = labels.to(self.device)

                    self.optimizer.zero_grad()

                    with torch.set_grad_enabled(phase == "train"): #訓練時に勾配を計算する
                        outputs = self.model(inputs)
                        _,preds = torch.max(outputs[1],1)
                        loss = self.criterion(outputs[1],outputs[0],labels)

                        if phase == 'train':
                            loss.backward()
                            self.optimizer.step()
                        
                    running_loss += loss.item() * inputs.size()
                    running_corrects += torch.sum(preds == labels.data)

                if phase == "train":
                    self.schedular.step()
                
                epoch_loss = running_loss / self.dataset_sizes[phase]
                epoch_acc = running_corrects.double() / self.dataset_sizes[phase]

                print(f"{phase} Loss: {epoch_loss:.4f} Acc: {epoch_acc:.4f}")

                #精度が良かったときのモデルの重みをコピー
                if phase == "test" and epoch_acc > best_acc:
                    best_acc = epoch_acc
                    best_model_wts = copy.deepcopy(self.model.state_dict())
            print()

        time_elapsed = time.time() - since
        print(f"Training complete in {(time_elapsed//60):.0f}m {(time_elapsed%60):.0f}")
        print(f"Best test Acc: {best_acc}")

        
        self.model.load_state_dict(best_model_wts)
        return self.model
    

    def save_model(self,pth_save_path:str,onnx_save_path:str):
        torch.save(self.model.state_dict(),pth_save_path)
        input_image = torch.zeros((1,3,224,224)).to(self.device)
        onnx.export(self.model,input_image,onnx_save_path)


                