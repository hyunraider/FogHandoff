package foghandoff.fog;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import static foghandoff.fog.FogMessages.Location;

@Slf4j
@Getter
@Setter
@Component
public class MembershipList {

    private String hostId;
    private ConcurrentSkipListMap<String, Member> membersMap;
    private Member master;

    public MembershipList() {
        membersMap = new ConcurrentSkipListMap<>();
        this.hostId = ""; //get ip through socket api and read port from appconfig
    }

    /**
    * Return all Members in the data structure
    */
    public synchronized List<Member> getAllEntries() {
        List<Member> allMembers = new ArrayList<>();
        for (Map.Entry<String, Member> member : membersMap.entrySet()) {
            allMembers.add(member.getValue());
        }
        return allMembers;    
    }

    /**
    * Return a map of the member ids to their locations
    */
    public synchronized Map<String, Location> getAllEntriesMap() {
        HashMap<String, Location> allMembers = new HashMap<>();
        for(Map.Entry<String, Member> member : membersMap.entrySet()) {
            allMembers.put(member.getValue().getId(), member.getValue().getLoc());
        }
        return allMembers;
    }

    /**
    * Get membership fog nodes that are under some threshold of difference to the target edge device
    * @param target: the eedge device of interest wee are looking at
    * @return nodes: List of members that might be next responsible for our target
    */
    public synchronized List<Member> getPotentialNextFogNodes(Member target) {
        return null;
    }

    /**
    * Get the next N fog nodes alphabetically from our membership list. Mainly for debugging/simulation purppses
    */
    public synchronized List<Member> getNextNEntries(int N) {
        String current = hostId;
        List<Member> nextNMembers = new ArrayList<>();
        if (membersMap.size() <= N + 1) {
            for (Map.Entry<String, Member> member : membersMap.entrySet()) {
                if (!member.getKey().equals(hostId)) {
                    nextNMembers.add(member.getValue());
                }
            }
            return nextNMembers;
        }
        while (nextNMembers.size() < N) {
            var nextEntry = membersMap.higherEntry(current);
            if (nextEntry == null) {
                nextEntry = membersMap.firstEntry();
            }
            nextNMembers.add(nextEntry.getValue());
            current = nextEntry.getKey();
        }
        return nextNMembers;
    }

    /**
    * Ge a random fog node from the list
    */
    public synchronized Member getRandomNeighbour(final List<Member> neighbours){
        return membersMap.values().stream().filter(member -> (member.getId()!=hostId && !neighbours.contains(member))).findAny().get();
    }

    public synchronized void remove(String id) {
        System.out.println("Removing Node with id " + id );
        membersMap.remove(id);
    }

    public synchronized void add(final String id, Location loc){
        membersMap.putIfAbsent(id, new Member(id, loc));
    }

    public synchronized void add(Member m){
        String id = m.getId();
        membersMap.putIfAbsent(id, m);
    }

    /**
    * Used for bootstrappping, add a whole bunch of nodes to the data structure. Ease of initialization.
    */
    public synchronized void addAll(final List<Member> members){
        members.forEach(member -> {
            membersMap.putIfAbsent(member.getId(),member);
        });
    }

    public String toString(){
        StringBuilder builder = new StringBuilder("Membership List (Size "+this.getMembersMap().size()+ ") : \n");
        this.membersMap.keySet().stream().forEach(key -> builder.append(key + "\n"));
        return builder.toString();
    }
}
