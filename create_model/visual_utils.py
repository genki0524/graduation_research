from torch import Tensor,nn
import numpy as np
import matplotlib.pyplot as plt
import torch
from torch.utils.data import dataloader

def imshow(inp:Tensor,title):
    inp_numpy : np.ndarray = inp.numpy().transpose((1,2,0))
    mean = np.array([0.485,0.456,0.406])
    std = np.array([0.229,0.224,0.225])
    plt.imshow(std * inp_numpy + mean)
    if title is not None:
        plt.title(title)
    plt.pause(0.001)

def visualize_model(model:nn.Module,class_names:list[str],device:str,num_images=6):
    was_training = model.training
    model.eval()
    images_so_far = 0
    fig = plt.figure()

    with torch.no_grad():
        for i, (inputs,labels) in enumerate(dataloader["test"]):
            inputs : Tensor = inputs.to(device)
            labels : Tensor = inputs.to(device)

            outputs = model(inputs)
            _,preds = torch.max(outputs[1],1)

            for j in range(inputs.size()[0]):
                images_so_far += 1
                ax = plt.subplot(num_images//2,2,images_so_far)
                ax.axis("off")
                ax.set_title(f"predicted: {class_names[preds[j]]}")
                imshow(inputs.cpu())

                if images_so_far == num_images:
                    model.train(mode=was_training)
                    return
        
        model.train(mode=was_training)