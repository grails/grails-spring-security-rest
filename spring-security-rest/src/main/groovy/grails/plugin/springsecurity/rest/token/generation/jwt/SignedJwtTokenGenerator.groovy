/*
 * Copyright 2013-2016 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
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
package grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean

/**
 * Generates JWT's protected using HMAC with SHA-256
 */
@Slf4j
@CompileStatic
class SignedJwtTokenGenerator extends AbstractJwtTokenGenerator implements InitializingBean {

    String jwtSecret

    JWSSigner signer

    RSAKeyProvider keyProvider

    JWSAlgorithm jwsAlgorithm

    @Override
    void afterPropertiesSet() throws Exception {
        if (JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)) {
            signer = new RSASSASigner(keyProvider.privateKey)
        } else {
            signer = new MACSigner(jwtSecret)
        }
    }

    @Override
    protected JWT generateAccessToken(JWTClaimsSet claimsSet) {
        JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .type(JOSEObjectType.JWT)
                .build()
        SignedJWT signedJWT = new SignedJWT(header, claimsSet)
        signedJWT.sign(signer)

        return signedJWT
    }

}
