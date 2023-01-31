package org.to2mbn.jmccc.mcdownloader.download.concurrent;

class WhateverCallback<T> implements Callback<T> {

    private Runnable proxied;

    public WhateverCallback(Runnable proxied) {
        this.proxied = proxied;
    }

    @Override
    public void done(T result) {
        proxied.run();
    }

    @Override
    public void failed(Throwable e) {
        proxied.run();
    }

    @Override
    public void cancelled() {
        proxied.run();
    }

}
