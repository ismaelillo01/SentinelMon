package monitoring.services;

public class Test {
    public static void main(String[] args) {
        CpuServices cpu = new CpuServices();
        Ramservices ram = new Ramservices();
        GpuServices gpu = new GpuServices();
        DiskServices disco = new DiskServices();

        System.out.println(cpu.getInfo());
        System.out.println(ram.getInfo());
        System.out.println(gpu.getInfo());
        System.out.println(disco.getSpace());
    }
}
