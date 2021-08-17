#-*- encoding: utf-8 -*-
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from pyfcm import FCMNotification
from uuid import uuid4
import pyrebase

import cv2
import numpy as np
import datetime
from time import time

APIKEY="AAAAalMeKts:APA91bEiB12GcGeo5W0MmzOjjmcDiR9LwrVgUxmspbWpI4eZz0LjuFIuTVxnfCbqd_IoeMjVkqJt5BGe9V77gvzFLmfSj5utQtj_0C0B0Y3LYM9nFytYpgDA_RV4HouwU-Qp7t8RwWMd"
#TOKEN="dfjVL2OCTOOZDLE0VSBV1Q:APA91bEaotIIYMcGb3SYu8_UZn9z3MLg9oQ8eOE6OINuBP6EyT7SXq9WbFGH5GRKLIRbR-cyAV_qV2c1vLRT_i_QI7IzxgUFFkBxxl--PjpimTxc0DJTV-y7APm59nDCGpghWzTaVO3C"
TOKEN="f-ky31r_T5GffjE1040WfT:APA91bFvyUcoSeW0HjlpZvpLgnp9NePgQmGdRCHqNd3K7XlS8EhFPy8YTSzVvPzzpApFlFwKCfaQzrD-cYzqGuXjw5Xt-CfU0GjtCCc0MYWjqud3F0un3n5DO1UpKHyICN-sklnuLuF-"
config={
	"apiKey":"kk4ks2sHVBKCA5TExxZPjEiqlNmJOdUywZN4At5g",
	"authDomain": "project-8965d.firebaseapp.com",
	"databaseURL": "https://project-8965d-default-rtdb.firebaseio.com",
	"storageBucket": "gs://project-8965d.appspot.com"
}
firebase=pyrebase.initialize_app(config)
db=firebase.database()
push_service = FCMNotification(api_key=APIKEY)
PROJECT_ID = "project-8965d"
cred = credentials.Certificate(
    "/home/pi/Downloads/project-8965d-firebase-adminsdk-bkl6z-29507732d7.json")

default_app = firebase_admin.initialize_app(cred, {'storageBucket': f"{PROJECT_ID}.appspot.com"})
# 버킷은 바이너리 객체의 상위 컨테이너. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너.
bucket = storage.bucket()  # 기본 버킷 사용

video=cv2.VideoCapture(-1)

fire_status=True
#previous=time()
delta=0

picture_directory = "/home/pi/image_store/"
def fire_detect():
    global fire_status,current,delta
    
    #감시는 무한 반복 하되 10초마다 사진을 찍음 
    while True:
       
        (grabbed, frame) = video.read()
        frame = cv2.resize(frame, (800, 600))
        if not grabbed:
            break
        blur = cv2.GaussianBlur(frame, (21, 21), 0)
        hsv = cv2.cvtColor(blur, cv2.COLOR_BGR2HSV)

        lower = [5, 44, 228]
        upper = [25, 255, 255]
        lower = np.array(lower, dtype="uint8")
        upper = np.array(upper, dtype="uint8")

        mask = cv2.inRange(hsv, lower, upper)
        
        conts,h=cv2.findContours(mask.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
        for contoura in conts:
            area = cv2.contourArea(contoura)
            if(area > 400):
                x,y,w,h = cv2.boundingRect(contoura)
                cv2.rectangle(frame, (x,y),(x+w,y+h),(0,0,255),2)
                cv2.putText(frame, 'Fire', (x+w, y), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (36,255,12), 2)
        output = cv2.bitwise_and(frame, hsv, mask=mask)
        no_red = cv2.countNonZero(mask)
        cv2.imshow("output", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            #여기에다 카메라 종료문을 선언하면 문제없음 
            cv2.destroyAllWindows()
            video.release()
            break
        #무한반복으로 불꽃연기를 감지하면서 만약 불꽃을 검출했을 때 10초에 한번씩 쉘창에 화재 감지 안내 및 사진 촬영 이벤트 지속
        #current=time()
        #previous=time()
        
        #아래 수치를 작게 할 수록 감지가 잘되는데 원본파일의 수치는 1만이었음,그리고 변수를 추가하여 10초 상태를 확인
        if int(no_red) > 1500 and fire_status==True:
        #10초마다 사진을 찍음(time func이용) //all event this here
            #if fire_status==True:
            
            print("fire detected!!!")
            
            now=datetime.datetime.now()
            str_now=str(now.strftime('%Y-%m-%d %H:%M:%S'))
            str_now_search=str(now.strftime('%Y%m%d%H%M%S'))
            filename=str_now + '.jpg'
            
            #10초마다 실행되는 이벤트. (푸쉬메시지도 걍 추가함(매번 화재가 발생하는게 아니라서 굳이 뺼 필요도 없음))
            sendMessage(str_now)
            savePhoto(now,frame,filename)
            uploadPhoto(str_now,str_now_search,filename)
            
            
            
            #탈출해서 시간 재야함 
            fire_status=False
            previous=time()
            
        #불꽃을 감지해서 10초가 지난 후에 이벤트 실행
        if fire_status==False:
            current=time()
            delta=delta+(current-previous)
            previous=current
            
            if delta >10:
                delta=0
                fire_status=True
                
                    
        #창이 두개 뜨는게 동작이 똑같아서 한개의 창만 띄움 
        #cv2.imshow("Detection", frame)
        #if cv2.waitKey(1) & 0xFF == ord('q'):
            #break
                    
    #q를 누르기 전 까지는 2개의 창이 절대 안꺼짐
    #코드에서 매번 불꽃을 감지할 때마다 새 창을 띄워놓으니.. 당연히 사진이 계속 찍히고 창이 계속 뜨는 것임
    
    
def savePhoto(now, frame,filename):
    cv2.imwrite(picture_directory+filename, frame)
    print('사진 저장 완료' +filename)
        
def sendMessage(now):
    data_message={
        "title":now,
        "body": "실내에 화재가 감지되었습니다."
        }
    push_service.single_device_data_message(registration_id=TOKEN, data_message=data_message)
    
def uploadPhoto(now,now_search,file):
    
    #파베 스토리지 주소: /pictures/ + file
    blob = bucket.blob('pictures/' + file)
    #아래 코드 한줄 추가하니깐 에러남. 나중에 다시 한번 주석 해제 해보고 실행해보자.
    #blob.make_public()
    
    # new token, metadata 설정
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token}  # access token 필요
    blob.metadata = metadata

    blob.upload_from_filename(filename=picture_directory+file)
    print("사진 업로드 완료")
    
    #공개 저장소 링크 디버깅 코드. (출력된 주소를 웹에 입력하면 사진뜹니다.)
    print(blob.public_url)
    
    
    #스토리지 이미지를 public으로 접근 권한 부여하여 db에 메타정보를 연동하여 이를 이용
    #search는 앱에서 검색하거나 정렬할 때 사용됨(202108입력하면 08월 사진 다뜸)
    db.child("image_Data").child(now).child("image").set(blob.public_url)
    db.child("image_Data").child(now).child("title").set(now)
    db.child("image_Data").child(now).child("description").set(now+'에 찍힌 사진입니다.')
    db.child("image_Data").child(now).child("search").set(now_search)
    
    
    
fire_detect()           
