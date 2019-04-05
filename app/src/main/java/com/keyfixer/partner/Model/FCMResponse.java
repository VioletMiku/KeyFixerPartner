package com.keyfixer.partner.Model;

import java.util.List;

public class FCMResponse {
    public long multicase_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;

    public FCMResponse() {
    }

    public FCMResponse(long multicase_id , int success , int failure , int canonical_ids , List<Result> results) {
        this.multicase_id = multicase_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.results = results;
    }

    public long getMulticase_id() {
        return multicase_id;
    }

    public void setMulticase_id(long multicase_id) {
        this.multicase_id = multicase_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
