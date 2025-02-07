from torch import nn as nn

class MobileNetV2WithCAP(nn.Module):
    def __init__(self,mobileNet_v2):
        super().__init__()
        self.features = mobileNet_v2.features
        self.avgpool = nn.AdaptiveMaxPool2d((1,1))
        self.classifier = mobileNet_v2.classifier
    
    def forward(self,x):
        x = self.features(x)
        x = self.avgpool(x)
        gap_output = x.view(x.size(0),-1)
        output = self.classifier(gap_output)
        return gap_output,output