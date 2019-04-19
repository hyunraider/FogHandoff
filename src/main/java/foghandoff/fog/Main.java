package foghandoff.fog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.SpringConfig;

@Slf4j
public class Main {
	
	// Persistent Components for our Fog Node
	private static FogNode fogNode;
	private static MembershipList membershipList;
	private static Predictor predictor;

	public static void main(String[] args) throws Exception {
		final ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);

		fogNode = ctx.getBean(FogNode.class);
		membershipList = ctx.getBean(MembershipList.class);

		final String predType = ctx.getEnvironment().getProperty("predictorType");
		if(predictorType == "cloud"){
            predictor = ctx.getBean(CloudPredictor.class);
        } else if (predictorType == "smart"){
            predictor = ctx.getBean(SmartPredictor.class);
        } else {
        	predictor = ctx.getBean(DumbPredictor.class);
        }
	}
}