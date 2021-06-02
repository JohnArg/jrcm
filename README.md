# jRCM - Java RDMA Communications Manager

### Table Of Contents
1. [What Is jRCM](#what-is-jrcm)
2. [Why Use jRCM](#why-use-jrcm)
2. [jRCM Components](#jrcm-components)

### What Is jRCM <a name="what-is-jrcm"></a>

jRCM is a Java library built for a Master Thesis
project, with title "Efficient State Machine Replication
With RDMA RPCs In Java". Its purpose is to make 
building RDMA RPCs for State Machine Replication (SMR)
in Java easier for developers, allowing flexibility as well.
For the purposes of the Master's Thesis,
jRCM was integrated to a forked repository of
[Hazelcast IMDG](https://github.com/JohnArg/hazelcast) to change
the RPCs of Hazelcast's Raft protocol to use RDMA networking
instead of TCP sockets.

### Why Use jRCM <a name="why-use-jrcm"></a>

Since RDMA requires a lot of
low level code, jRCM offers higher level constructs
that minimize the code needed 
to build RPCs for SMR with RDMA in Java.
Moreover, jRCM was created with a flexible design that
allows more than one way on building such RPCs.
jRCM is not an RPC framework though, as it
only deals with RDMA networking tasks and not with managing 
RPC requests and responses. This makes it easier to integrate
in an existing project that has its own way of managing RPC
requests and responses.
Additionally, jRCM is built on top of the
[DiSNI library](https://github.com/zrlio/disni) that enables
fast RDMA networking in Java with performance close to RDMA
networking in C.

### jRCM Components <a name="jrcm-components"></a>


jRCM has four main components that prepare any resources needed for 
RDMA data exchanges before starting the communications and 
retrieve those resources during communications.
These components are:

    1. A NetworkBufferManager that allocates and manages network buffers 
        for RDMA communications.
    2. An SVCManager that creates, stores and reuses SVCs 
        (Stateful Verb Calls) for performing RDMA operations efficiently.
    3. A WorkRequestProxyProvider that provides WorkRequestProxy objects 
        that represent Work Requests posted to the RDMA NIC.
    4. A WorkCompletionHandler interface to implement to specify what 
        will happen when an RDMA networking operation completes 
        succesfully or with errors.


The Stateful Verb Calls (SVCs) mentioned in (2) are a feature supported by
[DiSNI library](https://github.com/zrlio/disni)
that allows faster execution of JNI calls when performing RDMA
operations from Java. 
SVCs are used to post RDMA networking operations to the RDMA NIC through
JNI calls. An SVC saves the serialized state required for such a JNI call 
and can be reused to post the same RDMA networking operation to the 
RDMA NIC without repeating the serialization. This makes the overheads
from JNI calls during RDMA communications minimal and has allowed
RDMA communications in Java
to perform close to RDMA communications in C.
More information about this feature can be
found 
[here](https://dominoweb.draco.res.ibm.com/reports/rz3845.pdf).
Since jRCM is built
on top of DiSNI, this feature is available in jRCM as well.

There are multiple ways to build RPCs with RDMA for
an SMR system, so
jRCM was created with flexibility in mind. 
It allows developers to choose which RDMA operations to 
use for building their RPCs. It also provides the 
ActiveRdmaCommunicator endpoint for establishing point to point 
RDMA connections, which can be passed different implementations
of the jRCM components presented above 
(<i>Strategy Design Pattern</i>).


### How To Use jRCM

