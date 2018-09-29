# VICER_360ANDROID

## 소개
* Samsung Gear360 와 연동되는 Android Application으로, 수신받은 frame을 Streaming Server 로 보내준다.<br>
      <img src="./img/gear360.jpg">

#### 기능
* Gear360 에서 'Image stitching'된 Frame을 디코딩 후에 통신하기 적합한 형태로 인코딩하여 자체 제작된 Streaming Server로 본 프로토콜에서 요구되는 frame으로 추합하여 송신한다.

## 개발환경 설정 실행환경
Android Studio 2.3.3을 사용하여 Samsung Galaxy 7 환경으로 구현하였다. (complieSDK, targetSDK : 25 )

### Android Studio 설치
* 안드로이드 어플리케이션 개발 환경인 안드로이드 스튜디오를 설치하기 위해 아래의 사이트로 들어간다.
* https://developer.android.com/studio/?hl=ko

### JDK
* 다운 하고자 하는 JDK 를 설치하기 위해 아래의 Oracle홈페이지를 들어간다.
* https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
* Java SE Development Kit 8u181 에서 OS에 맞는 버전을 받아 sdk와 마찬가지로 External Tools의 jdk칸에 경로를 설정 해줍니다.


## 
