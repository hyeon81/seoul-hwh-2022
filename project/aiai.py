import cv2
import tensorflow.keras
import numpy as np
from keras_self_attention import SeqSelfAttention
from keras.models import load_model, Model

lists = []

## 이미지 전처리
def preprocessing(frame):
    # 사이즈 조정
    size = (224, 224)
    frame_resized = cv2.resize(frame, size, interpolation=cv2.INTER_AREA)
    
    # 이미지 정규화
    frame_normalized = (frame_resized.astype(np.float32) / 127.0) - 1
    
    # 이미지 차원 재조정 - 예측을 위해 reshape 해줍니다.
    frame_reshaped = frame_normalized.reshape((1, 224, 224, 3))
    
    return frame_reshaped


## 학습된 모델 불러오기
model_filename = 'AI/converted_keras/keras_model.h5'
model = load_model(model_filename, custom_objects={'SeqSelfAttention':SeqSelfAttention})

# for index in range(3):
#     lists.append(cv2.VideoCapture(index))

# # 캡쳐 프레임 사이즈 조절
# for capture in lists:
#     capture.set(cv2.CAP_PROP_FRAME_WIDTH, 320)
#     capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 240)

# sleep_cnt = 1 # 30초간 "졸림" 상태를 확인하기 위한 변수

def loadImage(frame):
    try:
        prediction = model.predict(preprocessing(cv2.flip(frame, 1)))
        list_setting = [prediction[0,0],prediction[0,1],prediction[0,2],prediction[0,3]]
        now = list_setting.index(max(list_setting))
        print(prediction)
        if(now == 0):
            return "cell"
        elif(now == 1):
            return 'paper_cup'
        elif(now == 2):
            return 'can'
        elif(now == 3):
            return 'pla'
        else:
            return 'gg...'
    except:
        print("EXCEPT CAM")
# 카메라 객체 반환

