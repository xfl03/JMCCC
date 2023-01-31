package org.to2mbn.jmccc.launch;

class ExitWaiter implements Runnable {

    private Process process;
    private ProcessListener listener;

    public ExitWaiter(Process process, ProcessListener listener) {
        this.process = process;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        int exitCode = process.exitValue();
        listener.onExit(exitCode);
    }

}
