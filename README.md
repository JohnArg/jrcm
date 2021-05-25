# jRCM - Java RDMA Communications Manager

### What Is jRCM

jRCM is a Java library built for a Master Thesis
project, with title "Efficient State Machine Replication in Java
With RDMA RPCs". Its purpose is to make 
building RPCs with RDMA for State Machine Replication Systems (SMR)
in Java easier for developers, allowing flexibility as well.
Since RDMA requires a lot of
low level code, jRCM offers higher level constructs
that developers can use to build RPCs for SMR with RDMA, thus
minimizing the code they have to write. 
Additionally, jRCM was created with a design that
allows flexibility in how to build such RPCs.
It is not an RPC framework, as it
only deals with RDMA networking tasks and not with managing 
RPC requests and responses. This makes it easier to integrate
in an existing project that has its own way of managing RPC
requests and responses and has its own data format for RPC packets.
For the purposes of the Master's Thesis,
jRCM was integrated to a forked repository of
[Hazelcast IMDG](https://github.com/JohnArg/hazelcast) to change
the RPCs of Hazelcast's Raft protocol to use RDMA networking 
instead of TCP sockets.

### Why Use jRCM

RDMA networking usually requires a lot of low level code, which
requires more effort from developers than socket programming.
Thus, higher level constructs are needed to make RDMA programming
easier.
The [DiSNI library](https://github.com/zrlio/disni) already offers 
high level abstractions for establishing connections and posting 
RDMA networking operations to the RDMA NICs. But there are still
other tasks that can be taken care of by a library and minimize
the work of developers. jRCM builds on top of DiSNI to take care
of these tasks as well. More specifically, it offers:
<a name="feature-list"></a>

    1. Network buffer allocation and management
    2. SVC (Stateful Verb Calls) creation and execution for various
        RDMA operations.
    3. WorkRequestProxy objects that can be used by an application
        for data exchanges with RDMA without low level code.
    4. A simple interface to implement to specify what will happen when 
        an RDMA networking operation completes succesfully or with errors.
    5. Communication endpoints that can be passed different implementations
        of the above features, enabling flexibility in how RDMA communications
        will be performed.

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
jRCM also takes care to prepare SVCs before communications and
reusing them when needed, 
without the requiring the user of the library to
write code about it.


Additionally, there are multiple ways to build RPCs with RDMA for
an SMR system, so
jRCM was created with flexibility in mind. 
It 
allows developers to choose which RDMA operations to 
use for building their RPCs. In order to be this flexible, jRCM 
uses the <i>Strategy Design Pattern</i>. It provides a 
communication endpoint that can be provided different implementations
of features 1-4 from the above [list](#feature-list).


### How Does It Work

