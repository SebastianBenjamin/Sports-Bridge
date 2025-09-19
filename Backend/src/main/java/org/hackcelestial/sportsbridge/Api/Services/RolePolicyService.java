package org.hackcelestial.sportsbridge.Api.Services;

import org.hackcelestial.sportsbridge.Api.Entities.SbPost;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.hackcelestial.sportsbridge.Enums.ApiUserRole;
import org.springframework.stereotype.Service;

@Service
public class RolePolicyService {
    public boolean canLike(SbUser actor, SbPost post) {
        if (post.getAuthor().getId().equals(actor.getId()) && post.getAuthor().getRole() == ApiUserRole.ATHLETE) {
            return false; // athletes cannot like their own posts
        }
        return true;
    }

    public boolean canPost(SbUser actor) {
        // Athletes, Coaches, Sponsors can post; generic USER cannot
        return actor.getRole() == ApiUserRole.ATHLETE || actor.getRole() == ApiUserRole.COACH || actor.getRole() == ApiUserRole.SPONSOR;
    }
}

