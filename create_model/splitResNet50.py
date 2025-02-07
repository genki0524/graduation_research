import torch.nn as nn
import torch

class SplitResNet50(nn.Module):
    
    def __init__(self, resnet50):
        super().__init__()
        self.cnn = nn.Sequential(*list(resnet50.children())[:-1])
        self.flatten = nn.Flatten()
        self.fc = resnet50.fc

    def forward(self, x: torch.Tensor):
        representation = self.flatten(self.cnn(x))
        output = self.fc(representation)
        return representation, output
