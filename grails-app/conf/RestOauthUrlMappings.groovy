class RestOauthUrlMappings {

    static mappings = {

        name oauth: "/oauth/${action}/${provider}"(controller: 'restOauth')

        "/oauth/access_token"(controller: 'restOauth', action: 'accessToken')

    }
}