## FogHandoff Simulator

## How to Run Fog Component

To configure, please change properties in the `foghandoffConfig` file.

 sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
 sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
 
 sudo yum install -y apache-maven
 
 // Go to the project directory
 mvn clean install
 
 // Go to target directory
To run use `java -cp foghandoff.jar foghandoff.fog.main`

## Dumb Prediction Module
As an edge device moves around it continuously scans signal strength of fog nodes around it.

When it decides to connect it will send a ConnectionMessage with type NEW to the fog node. The fog node will respond with an AcceptMessage. Computation begins.

As the edge moves it continues to scan for fog nodes. When it detects a new better fog node it sends a TaskMessage with type KILL to the old fog node and a ConnectionMessage with type NEW to the better fog node. The better fog node will respond as before and we loop the process.

## Smart Prediction Module
Initially an edge device makes a brand new connection in the same way as the dumb prediction module, by sending a ConnectionMessage witth type NEW to the fog node and the fog node responding with an AcceptMessage.

As the edge device moves it will send TaskMessage with type INFO and a Velocity to the fog node it is currently connected to. The fog node receives this message, utilizes it for prediction and will do one of two things.

- In the case where no handoff is occuring (i.e. no candidates) it will send a CandidateNodes message back to the edge device with the `exists` attribute set to 0

- In the case where handoff does occur the fog node receives a list of potential candidate fog nodes from the predictor to tell the edge device to handoff to. The fog node contacts each of these fog nodes by sending an ConnectionMessage with type PREPARE. The contacted fog node will allocate port and start a Clienthandler thread for that node and send back an AllocatedMessage containing the given port. The original fog node will then conglomerate a list of potential fog nodes and their respective ports in a Candidate object. It will then pass this list into a CandidateNodes message that it sends back to the edge device with the `exists` attribute set to 1.

When the edge device receives this message it will choose one of the fog nodes to switch off to based off of some criteria (maybe signal strength). All it then has to do is send over a ConnectionMessage as usual to the new fog node which should send back an AcceptMessage. Then the process repeats itself.

The fog nodes will start a ClientHandler thread when told to PREPARE and kill it after some time (10s default) if no edge device connects to it. The edge device should probably still send a KILL message to the old node (?).
