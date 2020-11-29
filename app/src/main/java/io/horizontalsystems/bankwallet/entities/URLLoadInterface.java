package io.horizontalsystems.bankwallet.entities;

public interface URLLoadInterface
{
    void onWebpageLoaded(String url, String title);
    void onWebpageLoadComplete();
}
