/**
 * ApiResponse 형식의 비동기 요청 실패를 나타낸다.
 */
class ApiRequestError extends Error {
    /**
     * 서버 오류 정보로 요청 오류를 생성한다.
     *
     * @param {string} message 사용자에게 표시할 오류 메시지
     * @param {string|null} errorCode 서버 오류 코드
     * @param {number} status HTTP 응답 상태 코드
     */
    constructor(message, errorCode, status) {
        super(message);
        this.name = 'ApiRequestError';
        this.errorCode = errorCode;
        this.status = status;
    }
}

/**
 * 메타 태그에서 CSRF 설정을 읽고, 토큰이 없으면 CSRF 쿠키를 확인한다.
 *
 * 헤더 이름을 서버가 제공하지 않은 경우에는 CSRF 헤더를 임의로 만들지 않는다.
  *
  * @returns {{headerName: string, token: string}} CSRF 헤더 이름과 토큰
 */
function getCsrfInfo() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const metaToken = tokenMeta?.content || '';
    const csrfCookieName = 'XSRF-TOKEN';
    const tokenCookie = document.cookie
        .split('; ')
        .find((cookie) => cookie.startsWith(`${csrfCookieName}=`));

    return {
            headerName: headerMeta?.content || '',
            token: metaToken || (tokenCookie
                ? decodeURIComponent(tokenCookie.substring(csrfCookieName.length + 1))
                : '')
        };
}

/**
 * 공통 헤더와 CSRF 토큰을 포함하여 ApiResponse 형식의 요청을 전송한다.
 * `json`에는 직렬화할 객체를, `body`에는 FormData 등의 본문을 전달한다.
 *
 * @param {string|URL} url 요청 URL
 * @param {RequestInit & {json?: unknown}} [options={}] fetch 요청 옵션
 * @returns {Promise<unknown>} 성공한 ApiResponse의 data
 * @throws {ApiRequestError} 응답이 JSON이 아니거나 ApiResponse가 실패한 경우
 */
async function apiRequest(url, options = {}) {
    const {json, headers: optionHeaders, ...requestOptions} = options;
    const headers = new Headers(optionHeaders || {});
    const csrfInfo = getCsrfInfo();
    headers.set('Accept', 'application/json');
    headers.set('X-Requested-With', 'XMLHttpRequest');
    if (csrfInfo.headerName && csrfInfo.token) {
            headers.set(csrfInfo.headerName, csrfInfo.token);
        }

    if (json !== undefined) {
        headers.set('Content-Type', 'application/json');
        requestOptions.body = JSON.stringify(json);
    }

    const response = await fetch(url, {
        ...requestOptions,
        credentials: 'same-origin',
        headers
    });
    const contentType = response.headers.get('content-type') || '';

    if (!contentType.toLowerCase().includes('application/json')) {
        throw new ApiRequestError(
            '서버에서 올바르지 않은 응답을 반환했습니다.',
            null,
            response.status
        );
    }

    let result;
    try {
        result = await response.json();
    } catch (error) {
        throw new ApiRequestError(
            '서버 응답을 읽지 못했습니다.',
            null,
            response.status
        );
    }

    if (!response.ok || result?.success !== true) {
        throw new ApiRequestError(
            result?.message || '요청을 처리하지 못했습니다.',
            result?.errorCode || null,
            response.status
        );
    }

    return result.data;
}

window.apiRequest = apiRequest;