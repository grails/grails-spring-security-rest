class RestOauthUrlMappings {

    static mappings = {

        name oauth: "/oauth/${action}/${provider}"(controller: 'oauth')

    }
}