package server.statistics;

public class ServerStatistics {
    private int requestCount;
    private long totalProcessingTime;
    private long maxProcessingTime;

    public synchronized void updateStats(long processingTime) {
        this.requestCount++;
        this.totalProcessingTime += processingTime;
        this.maxProcessingTime = Math.max(this.maxProcessingTime, processingTime);
    }

    public synchronized String getRequestCount() {
        return String.valueOf(this.requestCount);
    }

    public synchronized String getAvgProcessingTime() {
        return String.valueOf((this.totalProcessingTime / this.requestCount)/1000.0);
    }

    public synchronized String getMaxProcessingTime() {
        return String.valueOf(this.maxProcessingTime/1000.0);
    }
}