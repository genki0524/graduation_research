import asyncio
from websockets.server import serve
from websockets import connect
import numpy as np
import torch
import torch.nn.functional as F
import cv2
import json
from scipy.stats import entropy
classes = ["UP","DOWN","LEFT","RIGHT","FORWARD"]
net = cv2.dnn.readNetFromONNX("regularizedModel_2024-09-27.onnx")
async def echo(websocket,path):
    async for message in websocket:
        arr = np.asarray(bytearray(message), dtype=np.uint8)
        img = cv2.imdecode(arr, -1)
        img_pre_resize = img
        cv2.imshow('image', img_pre_resize)
        img = cv2.resize(img,(224,224))
        img = img.astype(np.float32)/255.0
        mean = np.array([0.485, 0.456, 0.406])
        std = np.array([0.229, 0.224, 0.225])
        img = (img-mean)/std
        img = img[:,:,::-1]
        img = img.transpose(2,0,1)
        img = np.expand_dims(img,0)
        net.setInput(img)
        output = net.forward()
        output = torch.from_numpy(output.astype(np.float32)).clone()
        output = F.softmax(output, dim=1)
        output = output.to('cpu').detach().numpy().copy()
        entropy_value = entropy(np.squeeze(output))
        await websocket.send(json.dumps({"pose":classes[np.argmax(output)]}))
        result = output[0][np.argmax(output)]
        if entropy_value > 0.5:
            result = "Nothing"
        else:
            result = classes[np.argmax(output)]
        print("--------------------")
        print("result: ",result)
        print(f"UP: ",output[0][0],"\nDOWN: ",output[0][1], "\nLEFT: ",output[0][2], " \nRIGHT: ",output[0][3], "\nFORWARD: ", output[0][4])
        print("--------------------")
        cv2.putText(img_pre_resize,classes[np.argmax(output)],(20, 50), cv2.FONT_HERSHEY_SIMPLEX, 1 , (255,0,0) ,2)
        cv2.waitKey(10)

async def main():
    async with serve(echo,"172.16.1.12",8008,max_size=100000000000000000):
        await asyncio.Future()  # run forever

asyncio.run(main())