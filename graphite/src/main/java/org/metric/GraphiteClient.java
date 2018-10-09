package org.metric;

import com.codahale.metrics.graphite.Graphite;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;


public class GraphiteClient {
    public static Queue<String> queue = new LinkedList<>();//队列

    private static final String host = "127.0.0.1";
    private static final int port = 64003;
    private static final InetSocketAddress address = new InetSocketAddress(host, port);

    public static void main(String[] arg) throws Exception{
        Date dNow=new Date(1538028943 * 1000L);
        SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sDateFormat.format(dNow);
        System.out.println(str);
        System.out.println(System.currentTimeMillis() / 1000L);
        test1();
    }

    public static void test1() throws Exception {

        final Graphite graphite = new Graphite(address);
        graphite.connect();
        boolean b = graphite.isConnected();
        if(b){
            System.out.println("=====连接成功=====");
            //graphite.send("wpk.windows.cpu", "60", 150);
            //graphite.flush();
            while(true){
                cpu(graphite);
                Thread.sleep(2000);
            }
        }else{
            System.out.println("=====连接失败=====");
        }
        graphite.close();
    }

    private static void cpu(Graphite graphite) throws SigarException, IOException {
        Sigar sigar = new Sigar();
        CpuInfo[] infos = sigar.getCpuInfoList();
        CpuPerc[] cpuList = sigar.getCpuPercList();
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            //String str = String.format("wpk.windows.cpu_Combined.cpu_%s",(i+1));
            //String cpuPer = CpuPerc.format(cpuList[i].getCombined()).replace("%","");
            //graphite.send(str, cpuPer, System.currentTimeMillis() / 1000L);
            sendCpuPercToGraphite(cpuList[i], graphite, i+1);
            graphite.flush();
        }
    }

    private static void sendCpuPercToGraphite(CpuPerc cpu, Graphite graphite, int num) throws IOException {
        Long time = System.currentTimeMillis() / 1000L;
        //CPU用户使用率
        String cpuUser = CpuPerc.format(cpu.getUser()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuUser",num), cpuUser, time);

        //CPU系统使用率
        String cpuSys = CpuPerc.format(cpu.getSys()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuSys",num), cpuSys, time);

        //CPU当前等待率
        String cpuWait = CpuPerc.format(cpu.getWait()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuWait",num), cpuWait, time);

        //CPU当前错误率
        String cpuNice = CpuPerc.format(cpu.getNice()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuNice",num), cpuNice, time);

        //CPU当前空闲率
        String cpuIdle = CpuPerc.format(cpu.getIdle()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuIdle",num), cpuIdle, time);

        //CPU总的使用率
        String cpuCombined = CpuPerc.format(cpu.getCombined()).replace("%","");
        graphite.send(String.format("wpk.windows.%s.cpu_%s","cpuCombined",num), cpuCombined, time);
    }
}
