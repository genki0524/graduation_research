import os
import numpy as np
import torch
from PIL import Image
def frame2torch(dir_path,transform): 
    files = os.listdir(dir_path)
    files.sort()
    frames = []
    for file in files:
        image = Image.open(os.path.join(dir_path,file))
        image = transform(image)
        frames.append(image)
    return torch.stack(frames)