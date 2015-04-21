/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package grails.plugin.springsecurity.rest.token.storage.jwt

import grails.plugin.springsecurity.rest.JwtService
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.test.mixin.TestFor
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification

/**
 * Created by @marcos-carceles on 03/03/15.
 */
@TestFor(JwtTokenStorageService)
class JwtTokenStorageServiceSpec extends Specification {

    JwtTokenStorageService service
    SignedJwtTokenGenerator tokenGenerator

    void setup() {
        service = new JwtTokenStorageService(jwtService: new JwtService(jwtSecret: 'fooo'*8))
        tokenGenerator = new SignedJwtTokenGenerator(jwtSecret: service.jwtService.jwtSecret, defaultExpiration: 60, jwtTokenStorageService: service)
        tokenGenerator.afterPropertiesSet()
    }

    def "JWT Token signatures are verified"() {
        given:
        AccessToken accessToken = tokenGenerator.generateAccessToken(new User('testUser', 'testPassword', []))

        when:
        UserDetails user = service.loadUserByToken(accessToken.accessToken)

        then:
        user.username == 'testUser'

        when:
        service.loadUserByToken(accessToken.accessToken.replaceAll(/...$/,'bar'))

        then:
        thrown(TokenNotFoundException)

    }
}
