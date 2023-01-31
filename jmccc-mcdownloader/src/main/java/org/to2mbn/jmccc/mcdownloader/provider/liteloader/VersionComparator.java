package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.util.Comparator;

class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String a, String b) {
        String[] splitedA = a.split("\\.");
        String[] splitedB = b.split("\\.");
        int minLength = Math.min(splitedA.length, splitedB.length);
        for (int i = 0; i < minLength; i++) {
            int comparing;

            try {
                comparing = Integer.parseInt(splitedA[i]) - Integer.parseInt(splitedB[i]);
            } catch (NumberFormatException e) {
                comparing = splitedA[i].compareTo(splitedB[i]);
            }

            if (comparing != 0) {
                return comparing;
            }
        }
        return splitedA.length - splitedB.length;
    }

}
