import re
import pandas as pd
import os
from torch.utils.data import Dataset
import frame2torch
import time
class CustomImageDataset(Dataset):
  def __init__(self,img_dir,transform):
    self.img_dir = img_dir
    self.transform = transform
    self.img_labels = []
    self.folder_name2label = {}
    target = "Train" if re.search("Train",img_dir) else "Test"
    df = pd.read_csv(img_dir+"_csv"+"/"+target+".csv")
    for index,row in df.iterrows():
      self.folder_name2label[row["video_id"]] = row["label_id"]
    for folder in os.listdir(img_dir):
      if folder != ".DS_Store":
        self.img_labels.append((self.img_dir+"/"+folder,self.folder_name2label[int(folder)]))      

  def __len__(self):
    return len(self.img_labels)
  
  def __getitem__(self,idx):
    start = time.time()
    img_path,label = self.img_labels[idx]
    frames = frame2torch.frame2torch(img_path,self.transform)
    end = time.time()
    print("処理時間",start-end)
    return frames,label