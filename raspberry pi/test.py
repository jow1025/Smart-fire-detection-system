import cv2 as cv
import numpy as np
 
cap = cv.VideoCapture('0')
 
# BackgroundSubtractorMOG2
fgbg = cv.createBackgroundSubtractorMOG2()

count = 0
kernel = np.ones((5,5),np.uint8)

while(cap.isOpened()):
    # ret: frame capture 결과(boolean)
    # frame: capture한 frame
    ret, frame = cap.read()
    if (ret):
        frame1 = cv.pyrDown(frame) 
        fgmask = fgbg.apply(frame) 
        
        vid = cv.medianBlur(fgmask,7) 
        prdown = cv.pyrDown(vid)

        #원본과 합침
        vid1 = cv.bitwise_and(frame1,frame1,mask=prdown) 

        hsv = cv.cvtColor(vid1,cv.COLOR_BGR2HSV)

        lower = [5, 50, 50]
        upper = [100, 255, 255]

        lower = np.array(lower, dtype="uint8")
        upper = np.array(upper, dtype="uint8")
        mask = cv.inRange(hsv, lower, upper)

        output = cv.bitwise_and(vid1, hsv, mask=mask)
        no_red = cv.countNonZero(mask)

        imgray = cv.cvtColor(output,cv.COLOR_BGR2GRAY)
        ret, thresh = cv.threshold(imgray,127,255,0)
        #팽창 단계
        result = cv.dilate(thresh,kernel,iterations = 3) 
        
        nlabels, labels, stats, centroids = cv.connectedComponentsWithStats(result)

        for index, centroid in enumerate(centroids):
            if stats[index][0] == 0 and stats[index][1] == 0:
                continue
            if np.any(np.isnan(centroid)):
                continue

        x, y, width, height, area = stats[index]
        centerX, centerY = int(centroid[0]), int(centroid[1])

        if area > 100:
            cv.rectangle(frame1, (x, y), (x + width, y + height), (0, 255, 0),3)

        cv.imshow('frame', frame1)

        if int(no_red) > 1000:
            count += 1
        k = cv.waitKey(30) & 0xFF
        if k == ord('q'):
            break
    else:
        break
 
cap.release()
cv.destroyAllWindows()

if count >100 :
    print('fire')