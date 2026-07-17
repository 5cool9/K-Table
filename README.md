# K-Table
서울여자대학교 구루2 안드로이드 K-Table 레포지토리


## Git Branch Strategy
- `main`: 배포 가능하도록 관리 브랜치
- `dev`: 개발 중인 코드 관리 브랜치
- `feature/{기능명}`: 기능 개발 브랜치

<br>

## Commit Convention
<table>
  <tr>
    <th width="150">타입</th>
    <th width="430">설명</th>
  </tr>
  <tr><td><code>Feat</code></td><td>새로운 기능 추가</td></tr>
  <tr><td><code>Fix</code></td><td>버그 수정</td></tr>
  <tr><td><code>Design</code></td><td>UI/UX 디자인 변경</td></tr>
  <tr><td><code>Refactor</code></td><td>코드 리팩토링</td></tr>
  <tr><td><code>Style</code></td><td>코드 스타일 변경</td></tr>
  <tr><td><code>Docs</code></td><td>문서 수정</td></tr>
  <tr><td><code>Chore</code></td><td>그 외 수정</td></tr>
  <tr><td><code>Init</code></td><td>초기 설정</td></tr>
</table>

```
type: 설명
```

### Subject
- 제목, 변경 내용에 대한 간단한 요약
- **50자 이내**로 작성
- 마침표나 특수문자는 사용하지 않음

#### 예시
```
Feat: 홈 화면에 이미지 추가
Fix: 텍스트가 화면에 출력되지 않는 문제 해결
```
<br>
