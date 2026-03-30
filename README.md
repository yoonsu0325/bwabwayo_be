# README.md

![Thumbnail](./resources/Thumbnail.png)

## **프로젝트 개요**

비대면 화상 중고거래 플랫폼 **봐봐요**는 실시간 화상 채팅을 통한 거래 검증으로 **택배거래 사기 문제를 예방**하고, 안전하고 신뢰성 있는 중고거래 환경을 제공합니다.

- **개발 기간** : 2025.07.07 ~ 2025.08.18 (7주)
- **플랫폼** : Web
- **개발 인원** : 6명 (프론트엔드 2명, 백엔드 4명)

## 팀원 구성

| <img src="./resources/장종원.jpg" alt="장종원" width="300"> | <img src="./resources/조우영.png" alt="조우영" width="300"> | <img src="./resources/전소슬.jpg" alt="전소슬" width="300"> | <img src="./resources/이재원.jpg" alt="이재원" width="300"> | <img src="./resources/배준수.jpg" alt="배준수" width="300"> | <img src="./resources/원윤서.jpg" alt="원윤서" width="300"> |
| --- | --- | --- | --- | --- | --- |
| 장종원(PM) | 조우영(FE) | 전소슬(FE) | 이재원(BE) | 배준수(BE) | 원윤서(BE) |
| - AI 챗봇, 검색엔진<br>- WebRTC<br>- 인프라 구축 | - 소켓통신, WebRTC<br>- 결제연동 | - 사용자 인증 및 인가<br>- 개인화 서비스 API 연동<br> | -인증/보안<br>- OAuth 연동<br>- User API 개발 | -상품 도메인<br>-검색엔진<br>-S3 관리<br>- 알림(SSE)<br> | - 채팅(Web Socket)<br>- 화면 디자인<br>- 인프라 구축 |

## 시스템 아키텍처
![시스템 아키텍처](./resources/Architecture.png)

## 기술 스택

### FE
<div align="center">

![Cursor](https://img.shields.io/badge/Cursor-00D4AA?style=for-the-badge&logo=cursor&logoColor=white)
![Visual Studio Code](https://img.shields.io/badge/Visual_Studio_Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white)<br>
![HTML5](https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS](https://img.shields.io/badge/css-663399?style=for-the-badge&logo=css&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) <br>
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white) 
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) <br>
![OpenVidu](https://img.shields.io/badge/OpenVidu-FF6B6B?style=for-the-badge&logo=webrtc&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5C3EE8?style=for-the-badge&logo=axios&logoColor=white)
![Socket.io](https://img.shields.io/badge/Socket.io-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![Zustand](https://img.shields.io/badge/Zustand-764ABC?style=for-the-badge&logo=redux&logoColor=white)


</div>

- **Language |** JavaScript, TypeScript 5.5.3
- **Runtime Environment |** Node.js v22.17.1
- **Framework |** Next.js 14.2.30 (React 18.2.0), Tailwind CSS 4
- **Library |** Zustand 5.0.6, Axios 1.11.0, OpenVidu Browser 2.29.0, Socket.io 4.8.1, STOMP.js 7.1.1, SockJs 1.6.1, Event Source Polyfill 1.0.31, TossPayments SDK 2.3.5, Swiper 11.2.10, React Slick 0.30.3, React Calendar 6.0.0, React Date Picker 12.0.1, Lucide React 0.539.0
- **IDE |** Visual Studio Code 1.99.3, Cursor 1.4.5

### BE
<div align="center">

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)<br>
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)<br>
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Qdrant](https://img.shields.io/badge/Qdrant-FF6B6B?style=for-the-badge&logo=qdrant&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-BC2E3D?style=for-the-badge&logo=hibernate&logoColor=white)<br>
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white)


</div>

- **Language |** Java 17 (OpenJDK 17.0.16)
- **Framework |** Spring Boot 3.5.3

- **Library |** Spring Security, Oauth2 Client, JWT, Spring Web, Srping WebSocket, Spring WebFlux, OpenVidu, Spring Data JPA, Spring Data Redis, QueryDSL 5.0.0, Spring Cloud AWS 2.2.6, SpringDoc OpenAPI 2.8.8, Lombok, Apache Lucene, Lucene Analysis Nori, Open Korean Text 2.3.1
- **Database |** MySQL 8.0.18, Redis 7.2,  Qdrant 1.15.1
- **IDE |** IntelliJ IDEA 2025.1.3 (Ultimate Edition)
- **Build Tool |** Gradle 8.14.3

### DevOps
<div align="center">

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)

</div>

- **AMI |** Ubuntu 22.04.4 LTS
- **Web Server**: Nginx 1.18.0 (Ubuntu)
- **Docker |** 28.3.2
- **Docker Compose |** v2.38.2
- **CI/CD |** Jenkins 2.516.1

### Collaboration
<div align="center">

![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitLab](https://img.shields.io/badge/GitLab-FC6D26?style=for-the-badge&logo=gitlab&logoColor=white)<br>
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)<br>
![Mattermost](https://img.shields.io/badge/Mattermost-0058CC?style=for-the-badge&logo=mattermost&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![Webex](https://img.shields.io/badge/Webex-049FD9?style=for-the-badge&logo=webex&logoColor=white)

</div>

## 기능 구성

### 메인
<img src="./resources/main.png" alt="main" width="600">

### 판매글 보기
<img src="./resources/post.png" alt="post" width="600">

### 판매글 작성
<img src="./resources/write.png" alt="write" width="600">

### 채팅하기
<img src="./resources/chat.png" alt="chat" width="600">

### 화상거래
<img src="./resources/videocall.png" alt="videocall" width="600">

### 리뷰 작성
<img src="./resources/review.png" alt="review" width="600">

### 내정보
<img src="./resources/my.png" alt="my" width="600">

### 구매이력 조회
<img src="./resources/purchase.png" alt="purchase" width="600">

## 프로젝트 산출물

### 화면설계서

[![화면설계서](./resources/Page.png)](https://www.figma.com/design/1TaIBDaoszdC5N1xcZBrlZ/%ED%8C%80-%ED%94%84%EB%A1%9C%ED%95%84-%EB%94%94%EC%9E%90%EC%9D%B8?node-id=0-1&m=dev&t=wCETQbmlYcp8vyBa-1)


### ERD

[![ERD.png](./resources/ERD.png)](https://www.erdcloud.com/d/hDa5k3BnFy7xr85oo)

### API 명세서
[![API명세서.png](./resources/API.png)](https://hip-water-dfc.notion.site/API-23332874d9b180a9be1bcd4f3ba71315)
