package com.odobo.grails.plugins.rest

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * TODO: write doc
 */
class FakeAuthenticationProvider implements AuthenticationProvider {

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authenticationToken = authentication as UsernamePasswordAuthenticationToken

        if (authenticationToken.principal.equals(authenticationToken.credentials)) {
            def tokenValue = generateToken()
            def auth = new RestAuthenticationToken(authenticationToken.principal, authenticationToken.credentials,
                                                           [new SimpleGrantedAuthority('ROLE_USER')], tokenValue)
            return auth
        } else {
            throw new BadCredentialsException("Bad credentials")
        }
    }

    @Override
    boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication))
    }

    private String generateToken() {
        SecureRandom prng = SecureRandom.getInstance("SHA1PRNG")

        String randomNum = new Integer(prng.nextInt()).toString()

        MessageDigest sha = MessageDigest.getInstance("SHA-1")
        byte[] result =  sha.digest(randomNum.getBytes())
        return hexEncode(result)
    }

    private String hexEncode(byte[] aInput){
        StringBuilder result = new StringBuilder();
        def digits = ['0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'].toArray();
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[ (b&0xf0) >> 4 ]);
            result.append(digits[ b&0x0f]);
        }
        return result.toString();
    }
}
