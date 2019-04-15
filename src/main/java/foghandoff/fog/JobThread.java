package foghandoff.fog

import java.util.concurrent.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Setter
@Getter
public class JobThread implements Runnable {

	private ArrayList<Float> data;
	private AtomicInteger timeStep;

	public JobThread(List<Float> data) {
		data = new ArrayList<>();
		timeStep = new AtomicInteger(0);
	}

	@Override
	public void run() {
		for(int i = 0; i < @Value("{jobIterations}")int jobIterations; i ++) {
			
		}
	}
}