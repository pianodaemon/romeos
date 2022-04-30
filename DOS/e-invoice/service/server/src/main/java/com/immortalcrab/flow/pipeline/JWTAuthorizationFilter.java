package com.immortalcrab.flow.pipeline;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.ServletException;
import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.function.Function;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";
    private final String PUB_KEY_PATH = "/pem/sso_key.pub";

    private boolean findJWTToken(HttpServletRequest request, HttpServletResponse res) {

        String authenticationHeader = request.getHeader(HEADER);
        return !(authenticationHeader == null || !authenticationHeader.startsWith(PREFIX));
    }

    private Claims extractClaims(HttpServletRequest request) {

        String pubKeyPath = System.getenv("PUB_KEY_PATH");
        if (pubKeyPath == null) {
            pubKeyPath = PUB_KEY_PATH;
        }

        try {
            String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");
            return Jwts.parser().setSigningKey(loadPublicKey(new FileInputStream(pubKeyPath))).parseClaimsJws(jwtToken).getBody();

        } catch (Exception ex) {
            throw new UnsupportedJwtException(ex.getMessage());
        }
    }

    public <T> T getClaimFromToken(final Claims claims, Function<Claims, T> claimsResolver) {

        return claimsResolver.apply(claims);
    }

    private static Key loadKey(InputStream in, Function<byte[], Key> keyParser) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!(line.contains("BEGIN") || line.contains("END"))) {
                    content.append(line).append('\n');
                }
            }
            byte[] encoded = Base64.decodeBase64(content.toString());
            return keyParser.apply(encoded);
        }
    }

    public static Key loadPublicKey(InputStream in) throws IOException, NoSuchAlgorithmException {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return loadKey(in, bytes -> {
            try {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
                return keyFactory.generatePublic(spec);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setUpSpringAuthentication(Claims claims) {

        /* HACK to heal the evident absent of authorities of the current SSO
           Due to java is not the center of the world and there is not everywhere */
        List<String> authorities = new ArrayList<>() {
            {
                add("ROLE_USER");
            }
        };

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            if (findJWTToken(request, response)) {
                Claims claims = extractClaims(request);
                setUpSpringAuthentication(claims);
            } else {
                SecurityContextHolder.clearContext();
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }

    }

}
