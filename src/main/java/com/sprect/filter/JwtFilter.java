package com.sprect.filter;

import com.sprect.service.jwt.JwtService;
import com.sprect.utils.DefaultString;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JwtFilter extends GenericFilterBean {
    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            String uri = ((HttpServletRequest) req).getRequestURI();
            String token = jwtService.resolveToken((HttpServletRequest) req);
            if (token != null) {
                String type = jwtService.getClaims(token).getHeader().getType();
                Authentication auth = null;
                switch (type) {
                    case "access":
                        auth = jwtService.getAuthentication(token);
                        break;
                    case "resetPassword":
                        if (uri.equals("/user/resetPasswordThroughEmail")) {
                            auth = jwtService.getAuthentication(token);
                        }
                        break;
                }
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            filterChain.doFilter(req, res);
        } catch (ExpiredJwtException e) {
            throw new JwtException(DefaultString.ACCESS_EXPIRED);
        } catch (JwtException e) {
            throw new JwtException(DefaultString.ACCESS_INVALID);
        }
    }
}
