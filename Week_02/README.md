# 作业1

>   写一段代码，使用 HttpClient 或 OkHttp 访问 [http://localhost:8801 ](http://localhost:8801/)，代码提交到 Github。

使用 [Apache HttpClient](https://hc.apache.org/httpcomponents-client-4.5.x/index.html) 访问 [http://localhost:8801 ](http://localhost:8801/)，代码如下：

```java
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {
    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8801");
            ResponseHandler<String> responseHandler = httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = httpResponse.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpClient.execute(httpGet, responseHandler);
            System.out.println(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

依赖：

```xml
<dependency>
  <groupId>org.apache.httpcomponents</groupId>
  <artifactId>httpcomponents-client</artifactId>
  <version>4.5.13</version>
  <type>pom</type>
</dependency>
```

# 作业2

根据上述自己对于 1 和 2 的演示，写一段对于不同 GC 的总结，提交到 Github。

### Serial 收集器

Serial 收集器是一个单线程的收集器，只会使用一个处理器或者一个收集线程完成垃圾收集工作。

Serial 收集器可以通过 `-XX:+UseSerialGC` 开启。

Serial 收集器垃圾回收时，必须暂停其他所有的工作线程，直到它垃圾收集结束，这就是所谓的 “Stop The World”（STW）。STW 是 JVM 在后台自动发起和自动完成的，因此在用户不可知、不可控的情形下把用户的工作线程全部都停掉了。

Serial 收集器在新生代采用**标记-复制**算法，在老年代采用**标记-清除-整理**算法。

与其他收集器的单线程相比，Serial 收集器的优点在于简单而高效，对于内存资源受限的环境，它是所有收集器里额外内存消耗最小的；对于单核处理器或者处理器核心较少的环境来说，Serial 收集器由于没有线程切换的开销，专心做垃圾收集可以获得最高的单线程收集效率，因此 Serial 收集器对于运行在客户端模式下的虚拟机来说是一个很好的选择。

Serial 收集器 CPU 利用率高，但是只能利用单核心进行垃圾回收，因此并不能充分发挥出多核处理器的优势。

### ParNew 收集器

ParNew 收集器是 Serial 收集器的多线程版本，除了同时使用多个线程进行垃圾收集之外，其他行为和 Serial 收集器完全一致，因此使用标记-复制算法。

ParNew 收集器是新生代收集器，主要与老年代收集器 CMS 收集器配合使用。事实上，能与 CMS 配合的新生代收集器只有 ParNew 和 Serial 收集器。

ParNew 收集器是启用 CMS 收集器之后默认的新生代收集器，启用方式：`-XX:+UseConcMarkSweepGC`；也可以通过 `-XX:+UseParNewGC`。但是 JDK9 之后，取消了通过 `-XX:+UseParNewGC` 启用 ParNew 收集器的方式，也就是说，ParNew 收集器只能通过 CMS 启用，从此以后，ParNew 专门和 CMS 配合进行垃圾回收。

### Parallel Scavenge 收集器

Parallel Scavenge 收集器与 ParNew 垃圾收集器非常相似，它是新生代收集器，也是能够并行收集的多线程收集器，使用标记-复制算法。

与 ParNew 收集器不同的是，Parallel Scavenge 收集器的目标是达到一个可控制的吞吐量（Throughput）。

-   `吞吐量 = 运行用户代码的时间 / (运行用户代码的时间 + 运行垃圾收集的时间)`。

Parallel Scavenge 收集器提供了 2 个参数精确控制吞吐量，分别是控制最大垃圾停顿时间的 `-XX:MaxGCPauseMillis` 以及直接设置吞吐量大小的 `-XX:GCTimeRatio`。

Parallel Scanvenge 收集器还可以通过 `-XX:+UseAdaptiveSizePolicy` 激活垃圾收集器的自适应的调节策略（GC Ergonomics），收集器会根据当前系统的运行情况，动态调整 `-Xmn`、`-XX:SurvivorRation` 等细节参数来提供最合适的停顿时间和最大的吞吐量，用户只需要配置基本的参数，例如 `-Xmx`、 `-XX:MaxGCPauseMillis` 或者 `-XX:GCTimeRatio`，这一点与 ParNew 收集器也有很大不同。

### Parallel Old 收集器

Parallel Old 收集器是 Parallel Scavenge 收集器的老年代版本，支持多线程并行收集。

并行垃圾收集器采用 Parallel Scavenge + Parallel Old 这一对组合进行垃圾收集，其中新生代采用 Parallel Scavenge 收集器，使用标记-复制算法，老年代采用 Parallel Old 收集器，使用标记-清除-整理算法。新生代和老年代都是多线程并行收集，因此都会导致 STW。

并行垃圾收集器可以通过 `-XX:+UseParallelGC` 启用，可以通过设置 `-XX:ParallelGCThreads=N` 设置 GC 线程数，其默认值是 CPU 核心数。JDK8 默认的垃圾收集器就是并行收集器。

并行收集器适用于多核服务器，主要目标是增加吞吐量，因为对系统资源的有效利用，能达到更高的吞吐量：

-   GC 期间，所有的 CPU 内核都在进行垃圾回收，所以总暂停时间更短；
-   GC 之间，没有 GC 线程运行，不会消耗任何系统资源。

### CMS 收集器

CMS 收集器是一款老年代收集器，使用标记-清除算法，目标是实现最短回收停顿时间，避免老年代垃圾收集时出现长时间的卡顿，其实现手段是：

-   不对老年代进行整理，而是使用空闲列表（free-lists）管理内存空间的回收。
-   CMS 回收的过程大部分工作都是与应用线程并发执行。

尽管 CMS 与应用线程并发执行，但是 GC 线程仍然会占用 CPU，默认情况下，CMS 使用的并发线程数等于 CPU 核心数的 1/4。

CMS 垃圾收集器的启用方式是：`-XX:+UseConcMarkSweepGC` 。

CMS 适用于降低 GC 停顿导致的系统延迟的场景。

CMS 收集的过程主要包括：

-   1 初始标记 Initial Mark【STW】
-   2 并发标记 Concurrent Mark
-   3 并发预清理 Concurrent Preclean 【卡表】
-   4 最终标记 Final Remark 【STW】
-   5 并发清除 Concurrent Sweep
-   6 并发重置 Concurrent Reset

尽管 CMS 能够减少停顿时间，但是 CMS 仍然存在一些缺点：

-   内存碎片问题：由于 CMS 不进行压缩，因此会产生内存碎片；
-   不可预知的暂停时间：由于浮动垃圾和内存碎片问题的存在，可能出现并发失败的问题，可能会导致 STW （Full GC），甚至是退化为 Serial Old 收集器。

### G1 收集器

G1 (Garbage First) 收集器，开创了收集器面向局部收集的设计思路和基于 Region 的内存布局形式。

G1 是一款面向服务端应用的的垃圾收集器。在 JDK 9，G1 取代了 Parallel Scavenge + Parallel Old 组合（JDK 8 默认的垃圾收集器），成为服务端模式下的默认垃圾收集器，而 CMS 也被声明为不推荐使用的收集器。JDK 8 可以通过设置 `-XX:+UseG1GC` 开启 G1。

G1 的设计目标是支持“停顿时间模型”，即支持指定在一个长度为 M 毫秒的时间片段内，消耗在垃圾收集上的时间大概率不超过 N 毫秒，支持将 STW 停顿时间和分布变成可预期且可配置的。

G1 通过基于 Region 的堆内存布局实现这个目标。尽管 G1 仍然是遵循分代收集理论设计，但是它不再坚持固定大小以及固定数量的分代区域划分，垃圾收集的范围也不再是要么是这个新生代（Minor GC），要么是老年代（Major GC），要么就是整个堆（Full GC），而是将连续的 Java 堆划分为多个大小相等的独立区域（Region），默认是2048个，每个 Region 都可以按需成为新生带的 Eden、Survivor 或者是老年代空间，G1 面向堆内存的任何部分来组成回收集（Collection Set）。

G1 的 Region 中有一类特殊的 Humongous 区域，专门用来存储大对象，只要大小超过了一个 Region 大小的一半的对象都是大对象。每个 Region 的大小可以通过 `-XX:G1HeapRegionSize` 设置（默认值是堆内存的 1/2000），取值范围为 1MB~32MB 且是 N 的 2 次幂。若大对象超过了整个 Region 的容量的话，将会使用多个连续的 Region 存储。

G1 建立可预测的停顿时间模型，是因为他将 Region 作为最小回收单元，每次收集都是收集 Region 大小的整数倍，有计划地避免进行全堆的收集。G1 可以跟踪每个 Region 的垃圾回收的价值大小，价值是指回收释放的空间量以及所需要的时间值，然后后台维护一个优先列表，每次根据用户通过 `-XX:MaxGCPauseMillis` 设定的允许的收集停顿时间，优先回收价值最大的那些 Region。

G1 收集的步骤：

-   1 新生代模式转移暂停（Evacuation Pause）：类似于其他新生代收集器的垃圾收集
-   2 并发标记（Concurrent Marking）
-   3 转移暂停：混合模式（Evacuation Pause (mixed)）

G1 的注意事项：

某些情况下，G1 如果触发了 Full GC，就会退化使用 Serial 收集器收集，暂停时间将达到秒级别。

### 常用 GC 组合

-   简单高效、低延迟：Serial 收集器 + Serial Old 收集器
-   多线程、最短停顿时间：ParNew 收集器 + CMS 收集器
-   吞吐量优先：Parallel Scavenge 收集器 + Parallel Old 收集器

### 并行 v.s. 并发

-   并行（Parallel）: 并行描述的是多条垃圾收集线程之间的关系，并行指多条垃圾收集线程进行垃圾收集，此时用户线程处于等待状态。
-   并发（Concurrent）：并发描述的是垃圾收集线程与用户线程的关系，并发指同一时间垃圾收集线程与用户线程一起运行。