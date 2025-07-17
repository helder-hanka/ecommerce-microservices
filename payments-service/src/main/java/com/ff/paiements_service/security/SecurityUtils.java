package com.ff.paiements_service.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class SecurityUtils { // 'final' pour empêcher l'extension, constructeur privé pour empêcher l'instanciation

    /**
     * Récupère l'ID de l'utilisateur authentifié depuis le contexte de sécurité.
     * @return L'ID de l'utilisateur sous forme de Long.
     * @throws IllegalStateException Si l'utilisateur n'est pas authentifié ou si l'ID n'est pas un Long.
     */
    public static Long getCurrentUserId() {
        // Récupérer l'objet d'authentification du SecurityContextHolder
        Object authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifier si l'authentification est présente et du bon type
        if (!(authentication instanceof UsernamePasswordAuthenticationToken authToken)) {
            // Vous pouvez lancer une exception ou retourner null selon votre logique métier
            throw new IllegalStateException("Aucun utilisateur authentifié ou type d'authentification inattendu.");
        }

        // L'ID utilisateur est stocké comme les "credentials" dans votre JwtAuthenticationFilter
        Object userId = authToken.getCredentials();

        if (userId instanceof Long) {
            return (Long) userId;
        } else {
            // Gérer le cas où le credentials n'est pas un Long (par exemple, si c'est un Integer ou autre)
            throw new IllegalStateException("L'ID utilisateur dans les credentials n'est pas de type Long.");
        }
    }
}