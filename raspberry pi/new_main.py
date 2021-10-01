#-*- encoding: utf-8 -*-
import pigpio
import numpy as np
import datetime
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
import cv2 as cv
from uuid import uuid4
from flask import Flask
from time import time
import threading
import serial
import pyrebase
import push_message as fcm
servoX=pigpio.pi()
servoY=pigpio.pi()
x=15
y=18
delta=0
previous=time()

print("main start")

ser=serial.Serial("/dev/ttyUSB0",9600)
config={
	"apiKey":"kk4ks2sHVBKCA5TExxZPjEiqlNmJOdUywZN4At5g",
	"authDomain": "project-8965d.firebaseapp.com",
	"databaseURL": "https://project-8965d-default-rtdb.firebaseio.com",
	"storageBucket": "gs://project-8965d.appspot.com"
}
firebase=pyrebase.initialize_app(config)
db=firebase.database()
PROJECT_ID = "project-8965d"
cred = credentials.Certificate(
    "/home/pi/2021_cap/fire_detect_opencv/cert_key/project-8965d-firebase-adminsdk-bkl6z-29507732d7.json")
default_app = firebase_admin.initialize_app(cred, {'storageBucket': f"{PROJECT_ID}.appspot.com"})
# 버킷은 바이너리 객체의 상위 컨테이너. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너.
bucket = storage.bucket()  # 기본 버킷 사용

print("firebase setup end")

fire_status = False
fireCascade = cv.CascadeClassifier('/home/pi/2021_cap/fire_detect_opencv/fire.xml')
video = cv.VideoCapture("http://192.168.105.25:8091/?action=stream")
# capture.set(cv.CAP_PROP_FRAME_WIDTH, 680)
# capture.set(cv.CAP_PROP_FRAME_HEIGHT, 480)
picture_directory = "/home/pi/image_store/"

print("camera setup end")

capture_time = datetime.datetime.now()
   
def fire_detect():
    global fire_status,current,delta,previous
    
    #감시는 무한 반복 하되 10초마다 사진을 찍음 
    while True:
       
        (grabbed, frame) = video.read()
        #frame = cv.resize(frame, (800, 600))
        if not grabbed:
            break
        blur = cv.GaussianBlur(frame, (21, 21), 0)
        hsv = cv.cvtColor(blur, cv.COLOR_BGR2HSV)

        lower = [5, 44, 228]
        upper = [25, 255, 255]
        lower = np.array(lower, dtype="uint8")
        upper = np.array(upper, dtype="uint8")

        mask = cv.inRange(hsv, lower, upper)
        
        conts,h=cv.findContours(mask.copy(),cv.RETR_EXTERNAL,cv.CHAIN_APPROX_NONE)
        for contoura in conts:
            area = cv.contourArea(contoura)
            if(area > 400):
                x,y,w,h = cv.boundingRect(contoura)
                cv.rectangle(frame, (x,y),(x+w,y+h),(0,0,255),2)
                cv.putText(frame, 'Fire', (x+w, y), cv.FONT_HERSHEY_SIMPLEX, 0.9, (36,255,12), 2)
        output = cv.bitwise_and(frame, hsv, mask=mask)
        no_red = cv.countNonZero(mask)
        cv.imshow("output", frame)
        if cv.waitKey(1) & 0xFF == ord('q'):
            #여기에다 카메라 종료문을 선언하면 문제없음 
            cv.destroyAllWindows()
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
            fcm.sendMessage(str_now)
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

def move_up():
    valY=servoY.get_servo_pulsewidth(y)
    if valY+300<=2400:
        valY+=300
        servoY.set_servo_pulsewidth(y,valY)

def move_down():
    valY=servoY.get_servo_pulsewidth(y)
    if valY-300>=600:
        valY-=300
        servoY.set_servo_pulsewidth(y,valY)
    
def move_left():
    valX=servoX.get_servo_pulsewidth(x)
    if valX+300<=2400:
        valX+=300
        servoX.set_servo_pulsewidth(x,valX)
    
def move_right():
    valX=servoX.get_servo_pulsewidth(x)
    if valX-300>=600:
        valX-=300
        servoX.set_servo_pulsewidth(x,valX)
    
def servoReset():
    servoX.set_servo_pulsewidth(x,1500)
    servoY.set_servo_pulsewidth(y,1500)

def savePhoto(now,frame,filename):
    cv.imwrite(picture_directory+filename, frame)
    print('사진 저장 완료 :' +filename)

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

def trans_data():
    while True:
        val=ser.readline().strip()
        val=val.decode()
        a=list(val.split())
        Co2_gas=int(a[0])
        print(Co2_gas,"ppm")
        db.child("data_a").child("1-set").set(Co2_gas)
        db.child("data_a").child("2-push").push(Co2_gas)
        
#         now = datetime.datetime.now()
#         str_now=str(now.strftime('%Y-%m-%d %H:%M:%S'))
#         data_message={
#             "title":str_now,
#             "body": "현재 CO2수치:"+str(Co2_gas)+"ppm"
#             }
#         push_service.single_device_data_message(registration_id=TOKEN, data_message=data_message)




        
           

#data trans fer from arduino-> rpi
print("serial setting start")
thread_serial_data=threading.Thread(target=trans_data,args=())
thread_serial_data.start()
print("serial setting end")


#def fire_detect():
#    global fire_status
#    while True:
#        if fire_status==False:
#            observe()
#        else:
#            time.sleep(10) #no effect -> background thread is running..
#            fire_status=False

# capture.release()
#     #out.release()
# cv.destroyAllWindows()

thread_fire_detect=threading.Thread(target=fire_detect,args=())
thread_fire_detect.start()

app = Flask(__name__)
@app.route('/')
def index():
    return 'Hello world'
@app.route('/r_left')
def robot_left():
    move_left()
    return 'robot left'
@app.route('/r_right')
def robot_right():
    move_right()
    return 'robot right'

@app.route('/r_forward')
def robot_forward():
    move_up()
    return 'robot up'

@app.route('/r_backward')
def robot_backward():
    move_down()
    return 'robot down'
@app.route('/auto_on')
def robot_reset():
    servoReset()
    return 'servo reset'

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')