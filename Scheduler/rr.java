import java.util.*;
import java.io.*;

public class rr2{

  public static void main(String[] args){

    String isVerbose = args[0];
    if (!isVerbose.equals("--verbose")){
      // only print results ....
    }
    else{
      String filename = args[1];
      File file = new File(filename);
      ArrayList<String> list = new ArrayList<String>();

      String readLine = "";
      try {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        while ((readLine = br.readLine()) != null) {
          list.add(readLine);
        } // end while
      } // end try
      catch (IOException e) {
        System.err.println("Error Happened: " + e);
      }
      String[] input = list.get(0).split("\\s+");
      int numProcesses = Integer.parseInt(input[0]);
      int track = 0;
      int inputTrack = 1;

      ArrayList<process> processArray = new ArrayList<>();

      while (track < numProcesses){ // stop when track = num proceses
        int state = 0;
        int a = 0;
        int c = 0;
        int b = 0;
        int m = 0;
        int number = 1;
        while (inputTrack < input.length && track < numProcesses){
          if (state == 0){
            if (input[inputTrack].indexOf('(') >= 0){
              a = Integer.parseInt(input[inputTrack].substring(1));
            }
            state++;
            inputTrack++;
          }
          if (state == 1){
            c = Integer.parseInt(input[inputTrack]);
            state++;
            inputTrack++;
          }
          if (state == 2){
            b = Integer.parseInt(input[inputTrack]);
            state++;
            inputTrack++;
          }
          if (state == 3){
            if (input[inputTrack].indexOf(')') >= 0){
              m = Integer.parseInt(input[inputTrack].substring(0, input[inputTrack].length()-1));
            }
            state = 0;
            inputTrack++;
            track++;
          }
          lab2 newlab = new lab2();
          process j = new process(a, c, b, m);
          processArray.add(j);

        }// end while
      }// end while
      ArrayList<process> originalArray = new ArrayList<>();

      for (process h: processArray){
        originalArray.add(h);
      }

      Collections.sort(processArray, new processComparator());

      for (int i = 0; i < processArray.size(); i++){
        processArray.get(i).name = "P" + Integer.toString(i+1);
      }
      // build linked lists of processes
      LinkedList<process> unstarted = new LinkedList<>();
      for (process h : processArray){
        unstarted.add(h);
      }

      // FCFS //
      scheduler s = new scheduler();
      s.blocked = new LinkedList<>();
      s.running = new LinkedList<>();
      s.terminated = new LinkedList<>();
      s.unstarted = unstarted;
      s.processArray = processArray;
      s.originalArray = originalArray;
      s.ready = new LinkedList<>();
      s.call();

    }
  }// end main

  public static class processComparator implements Comparator<process> {
      @Override
      public int compare(process o1, process o2) {
          return o1.arrivalTime - o2.arrivalTime;
      }
  }

  public static class scheduler{

    LinkedList<process> blocked;
    LinkedList<process> running;
    LinkedList<process> ready;
    LinkedList<process> unstarted;
    LinkedList<process> terminated;
    ArrayList<process> processArray;
    ArrayList<process> originalArray;

    LinkedList<process> newReadies;

    int cycle;
    ArrayList<String> randomN;
    int randomX = 0;
    int cpuBurstToRun = -1;
    int beginCPUBurstToRun;

    float totalCPUUseTime = 0;
    float totalIOUseTime = 0;

    public int randomOS(int X, int U){
      return 1 + (X % U);
    }
    public void call(){
      ArrayList<String> randomNums = new ArrayList<>();;
      try (BufferedReader br = new BufferedReader(new FileReader("randomLargeNumbers.txt"))) {
          String line;
          while ((line = br.readLine()) != null){
            randomNums.add(line);
          }
      }
      catch(Exception e){
        System.out.println("Error: " + e);
      }
      randomN = randomNums;
      int size = this.unstarted.size() + this.ready.size() + this.running.size() + this.blocked.size();

      System.out.println("The original input was: " + inputToString(originalArray));
      System.out.println("The (sorted) input is:  " + inputToString(processArray));
      System.out.println("");
      System.out.println("This detailed printout gives the state and remaining burst for each process");
      System.out.println("");
      while(!this.unstarted.isEmpty() || !this.ready.isEmpty() ||
           !this.running.isEmpty() || !this.blocked.isEmpty()){
      //for (int i = 0; i < 106; i++)  {
        newReadies = new LinkedList<>();
        this.printCycle();
        this.currentState();
        System.out.println("");
        this.doBlockedProcesses();
        this.doRunningProcesses();
        this.doUnstartedProcesses();

        newReadies = sortBlocked(newReadies);
        int i = 0;
        while(i < this.ready.size()){
          if (newReadies.contains(this.ready.get(i))){
            this.ready.remove(i);
          }
          i++;
        }
        int j = 0;
        while (j < newReadies.size()){
          this.ready.add(newReadies.get(j));
          j++;
        }
        this.doReadyProcesses();
      }
      System.out.println("The scheduling algorithm used was Round Robin");
      this.printProcessSummary();
      this.printAllSummary();
    }

    public String inputToString(ArrayList<process> o){
      String s = Integer.toString(o.size());
      for (process j : o){
        s += " (" + j.arrivalTime + " " + j.cpuBurst + " " + j.totalcpuTimeNeeded + " " + j.ioBurst + ") ";
      }
      return s;
    }
    public LinkedList<process> sortBlocked(LinkedList<process> blocked){
      LinkedList<process> sorted = new LinkedList<>();
      for (int i = 0; i < this.processArray.size(); i++ ){
        if (blocked.contains(this.processArray.get(i))){
          sorted.add(this.processArray.get(i));
        }
      }
      return sorted;
    }

    public void doBlockedProcesses(){
      //System.out.println("DID BLOCKED");

      // I/O burst = previous CPU burst * m
      if (!this.blocked.isEmpty()){
        // sort blocked
        this.totalIOUseTime++;
        this.blocked = this.sortBlocked(this.blocked);

        int i = 0;
        while (i < this.blocked.size()){
          if (this.blocked.get(i).remainingIOBurst <= 1){
            System.out.println("Blocked moved to ready: " + this.blocked.get(i).name);
            process j = this.blocked.get(i);
            this.blocked.get(i).ioTime++;
            //this.ready.add(j);
            this.newReadies.add(j);
            this.blocked.remove(i);
          }
          else{
            System.out.println("Blocked staying in blocked " + this.blocked.get(i).name);
            this.blocked.get(i).remainingIOBurst--;
            this.blocked.get(i).ioTime++;
            i++;
          }
        }
      }
    }

    public void doRunningProcesses(){
      // if CPU burst has not reached completion
      if (!this.running.isEmpty()){
        //System.out.println("DID RUNNING");
        this.totalCPUUseTime++;
        process j = this.running.get(0);
        //System.out.println("Doing running on process:  " + j.name);
        if(j.remainingCPUBurst <= 1){

            j.finishingTime = this.cycle-1;
            this.terminated.add(this.running.removeFirst());
        }
        else{
          if (j.RRCPUBurst <= 1){
            //System.out.println(j.name + " is being setnt to blocked");
            int multiplier = j.ioBurst;
            j.remainingIOBurst = multiplier * j.RRBeginCPU;
            j.RRQuantumCPUBurst--;
            j.RRCPUBurst--;
            j.remainingCPUBurst--;
            this.blocked.add(this.running.removeFirst());
          }
          else if (j.RRQuantumCPUBurst > 1){ // if cpu and quantum hasn't run out, all is fine
            j.RRQuantumCPUBurst--;
            j.RRCPUBurst--;
            j.remainingCPUBurst--;
          }
          else if (j.RRQuantumCPUBurst <= 1 ) // if quantum ran out, stage 2 hasn't add to ready
          {
            j.RRCPUBurst--;
            j.RRQuantumCPUBurst--;
            j.remainingCPUBurst--;
            //  this.ready.add(p);
            this.newReadies.add(this.running.removeFirst());
          }
          else{
            System.out.println("some condiiton is not accounted for");
          }
        }
      }
    }

    public void doUnstartedProcesses(){
      if (!this.unstarted.isEmpty()){
        //System.out.println("DID UNSTARTED");
        process rn = this.unstarted.getFirst();
        int timeRn = rn.arrivalTime;
        if (timeRn < cycle){
          //move to ready the ones that are in the same arrival time
          for (process h : this.unstarted){
            if (h.arrivalTime == timeRn){
              //System.out.println("Doing unstarted on process: " + h.name);
              //  this.ready.add(h);
              this.newReadies.add(h);
            }
          }
          for(int i = 0; i < this.newReadies.size(); i++){
            process j = this.newReadies.get(i);
            if (this.unstarted.contains(j)){
              j.turnAroundTime = this.cycle;
              this.unstarted.remove(j);
            }
          }
        }
      }
    }// end doUnstartedProcesses

    public void doReadyProcesses(){
      if (!this.ready.isEmpty()){
      // if running is empty
        //System.out.println("DID READY");
        if (this.running.isEmpty()){
          // move current ready process into running
          process j = this.ready.removeFirst();
          //j.remainingCPUBurst--;
          this.running.add(j);
          if (j.RRCPUBurst < 1){
          //  System.out.println(j.name + " RRCPUBurst: " + j.RRCPUBurst); // if it needs to be assigned
            int U = j.cpuBurst;
            j.RRCPUBurst = randomOS(Integer.parseInt(this.randomN.get(randomX)), U);
            j.RRBeginCPU = randomOS(Integer.parseInt(this.randomN.get(randomX)), U);

            //System.out.println("New RRCPU Burst for " + j.name + " " + "RR CPU Burst: " + j.RRCPUBurst + " RRBegin CPU: " + j.RRBeginCPU);
            randomX++;
            if (j.RRCPUBurst < 2){
              j.RRQuantumCPUBurst = j.RRCPUBurst;
            }
            else{
              j.RRQuantumCPUBurst = 2;
            }
            System.out.println(j.name + " Find burst when choosing ready process to run " + this.randomN.get(randomX));
          }
          else{
            //
            System.out.println("No new RRCPU burst for " + j.name + " RRCPUBurst: " + j.RRCPUBurst);
            if (j.RRCPUBurst < 2){
              j.RRQuantumCPUBurst = j.RRCPUBurst;
            }else{
              j.RRQuantumCPUBurst = 2;
            }
          }

        }
        for (process h : this.ready){
          h.waitingTime++;
        }

      }
    }// end doReadyProcesses

    public void printProcessSummary(){
      System.out.println("");
      for (int i = 0; i < this.processArray.size(); i++)
      {
        process h = this.processArray.get(i);
        System.out.println("Process " + i + ":");
        System.out.println("        (A,B,C,M)" + " = " +"(" + h.arrivalTime + ","+ h.cpuBurst + ","+ h.totalcpuTimeNeeded + ","+ h.ioBurst + ")");
        System.out.println("        Finishing time: " + h.finishingTime);
        System.out.println("        Turnaround time: " + (h.finishingTime - h.turnAroundTime+1));
        h.turnAroundTime = h.finishingTime - h.turnAroundTime+1;
        System.out.println("        I/O time: " + h.ioTime);
        System.out.println("        Waiting time: " + h.waitingTime);
      }
    }
    public void printAllSummary(){
      System.out.println("Summary Data: ");
      System.out.println("        Finishing time: " + (this.cycle-1));

      float totalwaitingTime = 0;
      float totalturnaroundtime = 0;
      float numProcesses = 0;
      for(process g : processArray){
        totalwaitingTime += g.waitingTime;
        totalturnaroundtime += g.turnAroundTime;
        numProcesses++;
      }

      System.out.println("        CPU Utilization: " + this.totalCPUUseTime/(this.cycle-1));
      System.out.println("        I/O Utilization: " + this.totalIOUseTime/(this.cycle-1));
      System.out.println("        Throughput: " + numProcesses/(this.cycle-1) * 100 + " per hundred cycles");
      System.out.println("        Average turnaround time: " + (totalturnaroundtime/numProcesses));
      System.out.println("        Average waiting time: " + totalwaitingTime/numProcesses );

    }
    public void printCycle(){
      System.out.print("Before cycle" + String.format("%10s", " " + Integer.toString(cycle) +  ":  "));
      cycle++;
    }

    public void currentState(){
      int totalNumProcesses = this.blocked.size() + this.running.size()
                              + this.unstarted.size() + this.ready.size()
                              + this.terminated.size();
      for (int i = 0; i < totalNumProcesses; i++){
        String processName = "P" + Integer.toString(i+1);
        for (process h : this.blocked){
          if (h.name.equals(processName)){
            System.out.print(String.format("%1$15s", "blocked  " +  Integer.toString(h.remainingIOBurst)));
          }
        }
        for (process h : this.running){
          if (h.name.equals(processName)){
            System.out.print(String.format("%1$15s", "running= RRQuantum  " +  Integer.toString(h.RRQuantumCPUBurst)));
            System.out.print(String.format("%1$15s", "  running=RRCPUBurst " +  Integer.toString(h.RRCPUBurst)));

          }
        }
        for (process h : this.unstarted){
          if (h.name.equals(processName)){
            System.out.print(String.format("%1$15s", "unstarted 0" ));
          }
        }
        for (process h : this.ready){
          if (h.name.equals(processName)){
            System.out.print(String.format("%1$15s", "ready  0" ));
          }
        }

        for (process h : this.terminated){
          if (this.terminated != null && !this.terminated.isEmpty()){
            if (h.name.equals(processName)){
              System.out.print(String.format("%1$15s", "terminated  0" ));
            }
          }
        }

      }
    }
  }





  public static class process{

    // fields I added
    String name;
    int remainingCPUBurst;
    int remainingIOBurst;

    int RRCPUBurst; // <--- calculated for cpu burst for cycle
    int RRQuantumCPUBurst; // <-- quantum restriction for cycle
    int RRBeginCPU; // <--- calculate for IO burst

    // a, b, c, m
    int arrivalTime;
    int cpuBurst;
    int totalcpuTimeNeeded;
    int ioBurst;

    // results
    int finishingTime;
    int turnAroundTime;
    int ioTime;
    int waitingTime;

    public process(int a, int b, int c, int m){
      this.arrivalTime = a;
      this.cpuBurst = b;
      this.totalcpuTimeNeeded = c;
      this.ioBurst = m;
      this.remainingCPUBurst = totalcpuTimeNeeded;
    }

    public int getArrivalTime(){
      return arrivalTime;
    }
  }

}
