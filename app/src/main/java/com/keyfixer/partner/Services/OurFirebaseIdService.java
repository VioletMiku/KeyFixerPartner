package com.keyfixer.partner.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Token;

public class OurFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        UpdateTokenToServer(refreshedToken);
    }

    private void UpdateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);
        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null){ // if already login, update the token
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }
}
