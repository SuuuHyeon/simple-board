// [⭐️ 핵심] 'app.js'가 로드될 때 모든 로직을 실행합니다.
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. 변수 및 상태 관리 ---

    // API의 기본 URL (Spring Boot 서버)
    const API_BASE_URL = 'http://localhost:8080';

    // AccessToken은 '휘발성'이므로 메모리에 변수로 저장합니다.
    let accessToken = null;

    // RefreshToken은 '영속성'이므로 localStorage에 저장합니다.
    // (실무에서는 HttpOnly 쿠키가 더 안전합니다)
    let refreshToken = localStorage.getItem('refreshToken');

    // UI 요소 DOM 캐싱
    // [⭐️ 추가] 수정 팝업(Modal) 관련 DOM 캐싱
    const updateModalEl = document.getElementById('update-post-modal');
    // Bootstrap 5의 Modal 객체를 '미리' 생성 (JS로 팝업을 띄우기 위함)
    const updateModal = new bootstrap.Modal(updateModalEl);
    const updateForm = document.getElementById('update-form');

    // --- 추가: 모달 내부 input 요소들 캐싱 (없어서 에러 발생) ---
    const updatePostId = document.getElementById('update-post-id');
    const updatePostTitle = document.getElementById('update-post-title');
    const updatePostContent = document.getElementById('update-post-content');


    const authSection = document.getElementById('auth-section');
    const boardSection = document.getElementById('board-section');
    const logoutButton = document.getElementById('logout-button');
    const nicknameDisplay = document.getElementById('nickname-display');
    const messageArea = document.getElementById('message-area');

    // 폼(Form)
    const signupForm = document.getElementById('signup-form');
    const loginForm = document.getElementById('login-form');
    const postForm = document.getElementById('post-form');

    // 목록
    const postList = document.getElementById('post-list');
    const loadPostsButton = document.getElementById('load-posts-button');


    // [⭐️⭐️⭐️ 추가] 페이지네이션 상태
    let currentPage = 0; // 현재 페이지 (0부터 시작)
    const PAGE_SIZE = 5; // 한 페이지에 5개씩

    // [⭐️ 추가] 페이지네이션 DOM 캐싱
    const paginationControls = document.getElementById('pagination-controls');

    // --- 2. 핵심 로직: "인증 헤더가 포함된 fetch" ---

    /**
     * [⭐️⭐️⭐️]
     * JWT 인증/재발급 흐름의 '심장'입니다.
     * * 1. AccessToken을 헤더에 담아 API를 요청합니다.
     * 2. 응답이 401(Unauthorized)이면, 'AccessToken'이 만료된 것입니다.
     * 3. 'RefreshToken'으로 '토큰 재발급(/reissue)'을 시도합니다.
     * 4. 새 토큰 발급에 성공하면, '새 AccessToken'으로 '원래 요청'을 재시도합니다.
     * 5. 재발급마저 실패하면, '로그아웃'시킵니다.
     */
    const fetchWithAuth = async (url, options = {}) => {

        // 1. 헤더에 'accessToken' 삽입
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };
        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        // 2. 1차 요청
        let response = await fetch(url, {...options, headers});

        // 3. [⭐️ 401 에러 감지] AccessToken이 만료된 경우
        if (response.status === 401) {
            console.log('AccessToken 만료. 재발급 시도...');

            // 4. 토큰 재발급 시도
            const reissueSuccess = await handleTokenReissue();

            if (reissueSuccess) {
                // 5. [재시도] 새 토큰으로 헤더를 갱신하여 '원래 요청'을 다시 보냄
                headers['Authorization'] = `Bearer ${accessToken}`;
                response = await fetch(url, {...options, headers});
            } else {
                // 6. 재발급 실패 시, 로그아웃 처리
                showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', true);
                handleLogout();
                // 에러를 발생시켜 .catch()로 이동
                throw new Error('Session expired');
            }
        }
        return response;
    };

    /**
     * [⭐️⭐️⭐️]
     * 토큰 재발급 API (/members/reissue) 호출
     */
    const handleTokenReissue = async () => {
        if (!refreshToken) {
            return false;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/members/reissue`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({refreshToken: refreshToken})
            });

            if (!response.ok) {
                return false;
            }

            const tokens = await response.json();

            // [⭐️ 핵심] 새 토큰으로 교체 (RTR)
            accessToken = tokens.accessToken;
            refreshToken = tokens.refreshToken;
            localStorage.setItem('refreshToken', refreshToken); // 새 RefreshToken 저장

            console.log('토큰 재발급 성공');
            return true;

        } catch (error) {
            console.error('재발급 실패:', error);
            return false;
        }
    };

    // --- 3. API 호출 함수들 ---

    /**
     * 회원가입 처리
     */
    const handleSignup = async (e) => {
        e.preventDefault(); // 폼의 기본 제출 동작(페이지 새로고침) 방지

        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const nickname = document.getElementById('signup-nickname').value;

        try {
            const response = await fetch(`${API_BASE_URL}/members/signup`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email, password, nickname})
            });

            if (response.status === 201) { // 201 Created
                showMessage('회원가입 성공! 로그인해주세요.', false);
                signupForm.reset(); // 폼 초기화
            } else {
                const errorData = await response.json();
                showMessage(`회원가입 실패: ${errorData.message}`, true);
            }
        } catch (error) {
            showMessage(`네트워크 오류: ${error.message}`, true);
        }
    };

    /**
     * 로그인 처리
     */
    const handleLogin = async (e) => {
        e.preventDefault();

        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            const response = await fetch(`${API_BASE_URL}/members/login`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email, password})
            });

            if (response.ok) {
                const tokens = await response.json();

                // [⭐️ 핵심] 토큰 저장
                accessToken = tokens.accessToken;
                refreshToken = tokens.refreshToken;
                localStorage.setItem('refreshToken', refreshToken); // '안전한 지갑'에 저장

                showMessage('로그인 성공!', false);
                loginForm.reset();
                updateUI(true); // UI를 '로그인' 상태로 변경
                loadPosts(); // 게시글 로드
            } else {
                const errorData = await response.json();
                showMessage(`로그인 실패: ${errorData.message || '이메일/비밀번호 불일치'}`, true);
            }
        } catch (error) {
            showMessage(`네트워크 오류: ${error.message}`, true);
        }
    };

    /**
     * 게시글 목록 불러오기
     */
    const loadPosts = async () => {
        try {
            // [⭐️ 변경] 쿼리 파라미터로 'page', 'size', 'sort' 전달
            const response = await fetchWithAuth(
                `${API_BASE_URL}/posts?page=${currentPage}&size=${PAGE_SIZE}&sort=id,desc`, {
                    method: 'GET'
                });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '게시글 로딩 실패');
            }

            // const posts = await response.json();
            // [⭐️ 변경] 응답이 List가 아닌 Page '객체'임
            const pageData = await response.json();
            displayPosts(pageData.content); // 화면에 게시글 그리기

            // 추가: 페이지네이션 렌더링 (paginationControls가 존재하면 호출)
            if (typeof displayPagination === 'function' && paginationControls) {
                displayPagination(pageData);
            }

        } catch (error) {
            // fetchWithAuth에서 재발급 실패 시 에러가 여기서 잡힘
            if (error.message !== 'Session expired') {
                showMessage(`게시글 로딩 실패: ${error.message}`, true);
            }
        }
    };

    /**
     * 새 게시글 작성
     */
    const handleCreatePost = async (e) => {
        e.preventDefault();

        const title = document.getElementById('post-title').value;
        const content = document.getElementById('post-content').value;

        try {
            // [⭐️ 핵심] '인증된 fetch' 사용
            const response = await fetchWithAuth(`${API_BASE_URL}/posts`, {
                method: 'POST',
                body: JSON.stringify({title, content})
            });

            if (response.status === 201) { // 201 Created
                showMessage('새 글이 작성되었습니다.', false);
                postForm.reset();
                loadPosts(); // 목록 새로고침
            } else {
                const errorData = await response.json();
                showMessage(`글 작성 실패: ${errorData.message}`, true);
            }
        } catch (error) {
            showMessage(`글 작성 오류: ${error.message}`, true);
        }
    };

    /**
     * 로그아웃 처리
     */
    const handleLogout = async () => {
        try {
            // [⭐️ 핵심] '인증된 fetch' 사용 (로그아웃도 인증된 사용자만 가능)
            await fetchWithAuth(`${API_BASE_URL}/members/logout`, {
                method: 'POST'
            });
        } catch (error) {
            // 토큰이 이미 만료된 상태에서 로그아웃 시도 시 에러가 날 수 있으나,
            // 클라이언트 입장에선 어차피 로그아웃된 것이므로 무시.
            console.warn("로그아웃 API 호출 중 오류 (무시 가능):", error.message);
        } finally {
            // [⭐️ 핵심] 클라이언트 측 토큰 제거
            accessToken = null;
            refreshToken = null;
            localStorage.removeItem('refreshToken');

            updateUI(false); // UI를 '로그아웃' 상태로 변경
            showMessage('로그아웃되었습니다.', false);
        }
    };

    /**
     * [⭐️⭐️⭐️ 추가] 게시글 삭제 처리
     */
    const handleDeletePost = async (postId) => {
        // "confirm"은 브라우저의 '확인/취소' 팝업입니다.
        // '확인'을 누르면 true, '취소'를 누르면 false를 반환합니다.
        if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
            return; // '취소'를 누르면 아무것도 안 함
        }

        try {
            // [⭐️ 핵심] 'DELETE' 메서드로 '인증된 fetch' 호출
            const response = await fetchWithAuth(`${API_BASE_URL}/posts/${postId}`, {
                method: 'DELETE'
            });

            if (response.status === 204) { // 204 No Content (삭제 성공)
                showMessage('게시글이 삭제되었습니다.', false);
                loadPosts(); // 목록 새로고침
            } else {
                const errorData = await response.json();
                showMessage(`삭제 실패: ${errorData.message}`, true);
            }
        } catch (error) {
            showMessage(`삭제 오류: ${error.message}`, true);
        }
    };

    /**
     * [⭐️⭐️⭐️ 추가] 수정 팝업(Modal)을 띄우는 함수
     */
    const handleShowUpdateModal = async (postId) => {
        try {
            console.log('postId: ', postId);
            // 1. [핵심] 팝업에 '기존 값'을 채워넣기 위해,
            //    '단일 게시글 조회' API를 먼저 호출합니다.
            const response = await fetchWithAuth(`${API_BASE_URL}/posts/${postId}`, {
                method: 'GET'
            });

            if (!response.ok) throw new Error('게시글 정보를 불러오지 못했습니다.');

            const post = await response.json();

            console.log('post값: ' + post.id, post.title, post.content);
            // 2. 팝업(Modal) 안의 폼(Form)에 기존 데이터를 채워넣습니다.
            updatePostId.value = post.id; // 👈 숨겨진 input에 ID 저장
            updatePostTitle.value = post.title;
            updatePostContent.value = post.content;

            // 3. Bootstrap Modal 팝업을 '수동'으로 띄웁니다.
            updateModal.show();

        } catch (error) {
            showMessage(`수정 창 열기 오류: ${error.message}`, true);
        }
    };

    /**
     * [⭐️⭐️⭐️ 추가] 팝업(Modal)에서 '저장하기' 버튼을 눌렀을 때
     */
    const handleUpdatePost = async (e) => {
        e.preventDefault(); // 폼 새로고침 방지

        // 1. 팝업 폼에서 '수정된' 값들을 가져옵니다.
        const postId = updatePostId.value;
        const title = updatePostTitle.value;
        const content = updatePostContent.value;

        try {
            // [⭐️ 핵심] 'PUT' 메서드로 '인증된 fetch' 호출
            const response = await fetchWithAuth(`${API_BASE_URL}/posts/${postId}`, {
                method: 'PUT',
                body: JSON.stringify({ title, content }) // 👈 수정 DTO 전송
            });

            if (response.ok) {
                showMessage('게시글이 수정되었습니다.', false);
                updateModal.hide(); // 팝업 닫기
                loadPosts(); // 목록 새로고침
            } else {
                const errorData = await response.json();
                showMessage(`수정 실패: ${errorData.message}`, true);
            }
        } catch (error) {
            showMessage(`수정 오류: ${error.message}`, true);
        }
    };





    // --- 4. UI 헬퍼 함수들 ---

    /**
     * 게시글 목록을 HTML로 그려주는 함수
     */
    const displayPosts = (posts) => {
        postList.innerHTML = ''; // 목록 초기화

        if (posts.length === 0) {
            postList.innerHTML = '<p class="text-center text-muted">표시할 게시글이 없습니다.</p>';
            return;
        }

        posts.forEach(post => {
            const postEl = document.createElement('div');
            postEl.className = 'post-item';

            // [⭐️ 변경] 버튼이 포함된 HTML로 수정
            postEl.innerHTML = `
                <h6>${post.title}</h6>
                <p>${post.content}</p>
                <small>by ${post.authorNickname}</small>
                
                <!-- 수정/삭제 버튼 -->
                <div class="post-actions">
                    <!-- [⭐️ 핵심] data-post-id 속성에 'ID'를 심어둡니다. -->
                    <button class="btn btn-sm btn-outline-secondary update-button" data-post-id="${post.id}">
                        수정
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-button" data-post-id="${post.id}">
                        삭제
                    </button>
                </div>
            `;
            postList.appendChild(postEl);
        });
    };

    /**
     * 로그인 상태에 따라 UI를 변경하는 함수
     */
    const updateUI = (isLoggedIn) => {
        if (isLoggedIn) {
            authSection.style.display = 'none';
            boardSection.style.display = 'block';
            logoutButton.style.display = 'block';

            // (보너스) 토큰에서 닉네임 추출 (간단한 디코딩)
            try {
                const payload = JSON.parse(atob(accessToken.split('.')[1]));
                // 우리는 email을 subject에 넣었음 (닉네임은 id로 찾아야 하지만, 여기선 email로 대체)
                nicknameDisplay.textContent = payload.sub.split('@')[0];
            } catch (e) {
                nicknameDisplay.textContent = '사용자';
            }

        } else {
            authSection.style.display = 'block';
            boardSection.style.display = 'none';
            logoutButton.style.display = 'none';
            postList.innerHTML = ''; // 로그아웃 시 목록 비우기
        }
    };

    /**
     * 사용자에게 메시지를 보여주는 함수
     */
    const showMessage = (message, isError = false) => {
        messageArea.textContent = message;
        messageArea.className = `alert ${isError ? 'alert-danger' : 'alert-success'}`;
        messageArea.style.display = 'block';

        // 3초 뒤에 메시지 숨김
        setTimeout(() => {
            messageArea.style.display = 'none';
        }, 3000);
    };

    // --- 5. 초기화 ---

    // 이벤트 리스너 바인딩
    signupForm.addEventListener('submit', handleSignup);
    loginForm.addEventListener('submit', handleLogin);
    postForm.addEventListener('submit', handleCreatePost);
    logoutButton.addEventListener('click', handleLogout);
    loadPostsButton.addEventListener('click', loadPosts);
    updateForm.addEventListener('submit', handleUpdatePost); // [⭐️ 추가] 수정 폼 '저장' 이벤트


    postList.addEventListener('click', (e) => {
        // e.target은 '내가 실제로 클릭한 요소'

        // 1. '삭제' 버튼을 클릭했다면?
        if (e.target.classList.contains('delete-button')) {
            // e.target.dataset.postId는 HTML의 'data-post-id' 값을 가져옵니다.
            const postId = e.target.dataset.postId;
            handleDeletePost(postId);
        }

        // 2. '수정' 버튼을 클릭했다면?
        if (e.target.classList.contains('update-button')) {
            const postId = e.target.dataset.postId;
            handleShowUpdateModal(postId);
        }
    });

    // [⭐️⭐️⭐️ 추가] 페이지네이션 버튼 이벤트 위임
    paginationControls.addEventListener('click', (e) => {
        e.preventDefault(); // a 태그의 기본 동작(페이지 이동) 방지

        // 클릭된 요소가 .page-link 클래스를 가졌고, data-page 속성이 있다면
        if (e.target.classList.contains('page-link') && e.target.dataset.page) {
            const page = parseInt(e.target.dataset.page); // 0-based
            if (page >= 0 && page < 999) { // (간단한 유효성 검사)
                currentPage = page; // '현재 페이지' 상태 변경
                loadPosts(); // 해당 페이지로 다시 로드
            }
        }
    });

    // [⭐️ 핵심] 페이지 로드 시, 'RefreshToken'이 있으면 "자동 로그인 (재발급)" 시도
    if (refreshToken) {
        console.log('기존 RefreshToken 발견. 자동 재발급 시도...');
        handleTokenReissue().then(success => {
            if (success) {
                updateUI(true);
                loadPosts();
                showMessage('세션이 복원되었습니다.', false);
            } else {
                // RefreshToken이 유효하지 않은 경우
                handleLogout(); // 로컬 스토리지 정리
            }
        });
    }

    /**
     * [⭐️⭐️⭐️ 추가] 페이지네이션 버튼을 그리는 함수
     */
    const displayPagination = (pageData) => {
        paginationControls.innerHTML = ''; // 버튼 영역 초기화

        const totalPages = pageData.totalPages; // 총 페이지 수
        const currentPageNumber = pageData.number; // 현재 페이지 번호 (0-based)

        // '이전' 버튼
        const prevLi = document.createElement('li');
        prevLi.className = `page-item ${pageData.first ? 'disabled' : ''}`; // 첫 페이지면 'disabled'
        prevLi.innerHTML = `<a class="page-link" href="#" data-page="${currentPageNumber - 1}">이전</a>`;
        paginationControls.appendChild(prevLi);

        // 페이지 번호 버튼 (최대 5개만 보이게 간단히 처리)
        let startPage = Math.max(0, currentPageNumber - 2);
        let endPage = Math.min(totalPages - 1, currentPageNumber + 2);

        if (currentPageNumber < 2) {
            endPage = Math.min(totalPages - 1, 4);
        }
        if (currentPageNumber > totalPages - 3) {
            startPage = Math.max(0, totalPages - 5);
        }

        for (let i = startPage; i <= endPage; i++) {
            const pageLi = document.createElement('li');
            pageLi.className = `page-item ${i === currentPageNumber ? 'active' : ''}`; // 현재 페이지면 'active'
            pageLi.innerHTML = `<a class="page-link" href="#" data-page="${i}">${i + 1}</a>`; // (i + 1)은 사용자에게 보여줄 숫자
            paginationControls.appendChild(pageLi);
        }

        // '다음' 버튼
        const nextLi = document.createElement('li');
        nextLi.className = `page-item ${pageData.last ? 'disabled' : ''}`; // 마지막 페이지면 'disabled'
        nextLi.innerHTML = `<a class="page-link" href="#" data-page="${currentPageNumber + 1}">다음</a>`;
        paginationControls.appendChild(nextLi);
    };
});