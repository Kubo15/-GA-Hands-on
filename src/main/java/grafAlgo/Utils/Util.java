package grafAlgo.Utils;

import java.util.Collections;
import java.util.List;

public class Util {
	
	public static long calculateMax(List<Long> list){
		return Collections.max(list);
	}
	
	public static long calculateAverage(List<Long> list){
		long sum = 0;
		for(long element : list){
			sum+=element;
		}
		return sum/list.size();
	}

}
