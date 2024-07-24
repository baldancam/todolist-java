package com.mcbcode.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mcbcode.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Verificar a Rota
        var servletPath = request.getServletPath();
        
        if(servletPath.startsWith("/tasks/")) {

             // Pegar a autenticação (usuário e senha)      
            var authorization = request.getHeader("Authorization");
            
            if (authorization == null || !authorization.startsWith("Basic ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or invalid");
                return;
            }

            var authEncoded = authorization.substring("Basic".length()).trim();
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecode);

            String[] credentials = authString.split(":");
            if (credentials.length != 2) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header format");
                return;
            }

            String username = credentials[0];
            String password = credentials[1];

            // Validar Usuário
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            } else {
                // Validar Senha
                var passwordVerifyer = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passwordVerifyer.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
