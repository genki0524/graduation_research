import os
import shutil
dir_path = "./extracted_Test"
output_path = "./reducated_Test"

folders = os.listdir(dir_path)
cnt = 0
print(int(len(folders)/5))
for folder in folders:
    if folder != ".DS_Store" and folder != ".tmp.driveupload":
        shutil.copytree(dir_path+"/"+folder,output_path+"/"+folder)
        cnt += 1
        if cnt == int(len(folders)/5):
            break




