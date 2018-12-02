package edu.lu.uni.serval.faultlocalization;

/**
 * Metrics of computing suspicious value of suspicious lines.
 * 
 * https://hal.inria.fr/hal-01018935/document
 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045
 * 
 * @author kui.liu
 *
 */
public class Metrics {
	
	public Metric generateMetric(String metricStr) {
		Metric metric = null;
		if (metricStr.equals("Ample")) {
			metric = new Ample();
		} else if (metricStr.equals("Anderberg")) {
			metric = new Anderberg();
		} else if (metricStr.equals("ArithmeticMean")) {
			metric = new ArithmeticMean();
		} else if (metricStr.equals("Barinel")) {
			metric = new Barinel();
		} else if (metricStr.equals("Dice")) {
			metric = new Dice();
		} else if (metricStr.equals("DStar")) {
			metric = new DStar();
		} else if (metricStr.equals("Euclid")) {
			metric = new Euclid();
		} else if (metricStr.equals("Fagge")) {
			metric = new Fagge();
		} else if (metricStr.equals("Fleiss")) {
			metric = new Fleiss();
		} else if (metricStr.equals("GeometricMean")) {
			metric = new GeometricMean();
		} else if (metricStr.equals("Goodman")) {
			metric = new Goodman();
		} else if (metricStr.equals("Gp13")) {
			metric = new Gp13();
		} else if (metricStr.equals("Hamann")) {
			metric = new Hamann();
		} else if (metricStr.equals("Hamming")) {
			metric = new Hamming();
		} else if (metricStr.equals("HarmonicMean")) {
			metric = new HarmonicMean();
		} else if (metricStr.equals("Jaccard")) {
			metric = new Jaccard();
		} else if (metricStr.equals("Kulczynski1")) {
			metric = new Kulczynski1();
		} else if (metricStr.equals("Kulczynski2")) {
			metric = new Kulczynski2();
		} else if (metricStr.equals("M1")) {
			metric = new M1();
		} else if (metricStr.equals("M2")) {
			metric = new M2();
		} else if (metricStr.equals("McCon")) {
			metric = new McCon();
		} else if (metricStr.equals("Minus")) {
			metric = new Minus();
		} else if (metricStr.equals("Muse")) {
			metric = new Muse();
		} else if (metricStr.equals("Naish1")) {
			metric = new Naish1();
		} else if (metricStr.equals("Naish2")) {
			metric = new Naish2();
		} else if (metricStr.equals("Ochiai")) {
			metric = new Ochiai();
		} else if (metricStr.equals("Ochiai2")) {
			metric = new Ochiai2();
		} else if (metricStr.equals("Overlap")) {
			metric = new Overlap();
		} else if (metricStr.equals("Qe")) {
			metric = new Qe();
		} else if (metricStr.equals("RogersTanimoto")) {
			metric = new RogersTanimoto();
		} else if (metricStr.equals("Rogot1")) {
			metric = new Rogot1();
		} else if (metricStr.equals("Rogot2")) {
			metric = new Rogot2();
		} else if (metricStr.equals("RussellRao")) {
			metric = new RussellRao();
		} else if (metricStr.equals("Scott")) {
			metric = new Scott();
		} else if (metricStr.equals("SimpleMatching")) {
			metric = new SimpleMatching();
		} else if (metricStr.equals("Sokal")) {
			metric = new Sokal();
		} else if (metricStr.equals("SorensenDice")) {
			metric = new SorensenDice();
		} else if (metricStr.equals("Tarantula")) {
			metric = new Tarantula();
		} else if (metricStr.equals("Wong1")) {
			metric = new Wong1();
		} else if (metricStr.equals("Wong2")) {
			metric = new Wong2();
		} else if (metricStr.equals("Wong3")) {
			metric = new Wong3();
		} else if (metricStr.equals("Zoltar")) {
			metric = new Zoltar();
		}
		return metric;
	}

	public interface Metric {
		/**
		 * @param ef: number of executed failing test cases.     a_11
		 * @param ep: number of executed passing test cases.     a_10
		 * @param nf: number of un-executed failing test cases.  a_01
		 * @param np: number of un-executed passing test cases.  a_00
		 * @return
		 */
		double value(double ef, double ep, double nf, double np);
	}
	
	/**
	 * https://www.st.cs.uni-saarland.de/~dallmeier/pub/papers/ecoop05.pdf
	 * https://ieeexplore-ieee-org.proxy.bnl.lu/abstract/document/4041886/
	 * 
	 * @author kui.liu
	 *
	 */
	private class Ample implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return Math.abs(ef / (ef + nf) - ep / (ep + np));
		}
	}
	
	/**
	 * http://haslab.uminho.pt/sites/default/files/ruimaranhao/files/jss09.pdf
	 * 
	 * @author kui.liu
	 *
	 */
	private class Anderberg implements Metric {
		public double value(double ef, double ep, double nf, double np) {
	        return ef / (ef + 2 * (ep + nf));
	    }
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * 
	 * @author kui.liu
	 */
	private class ArithmeticMean implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return 2 * (ef * np - nf * ep) / ((ef + ep) * (np + nf) + (ef + nf) * (ep + np));
		}
	}
	
	/**
	 * http://haslab.uminho.pt/sites/default/files/ruimaranhao/files/ase09-1.pdf
	 * 
	 * @author kui.liu
	 *
	 */
	private class Barinel implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return 1 - ep / (ep + ef);
		}
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Dice implements Metric {
		public double value(double ef, double ep, double nf, double np) {
	        return 2 * ef / (ef + ep + nf);
	    }
	}
	
	/**
	 * https://ieeexplore-ieee-org.proxy.bnl.lu/stamp/stamp.jsp?tp=&arnumber=6651713
	 * http://www.utdallas.edu/~ewong/SE6367/01-Project/08-SFL-papers/06-DStar.pdf
	 * 
	 * @author kui.liu
	 *
	 */
	private class DStar implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef / (ep + nf);
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045
	 * @author kui.liu
	 *
	 */
	private class Euclid implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return Math.sqrt(ef + np);
		}
		
	}

	private class Fagge implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	Double _false = Double.valueOf(ef + nf);
	    	if (_false.compareTo(0d) == 0) {
	    		return 0d;
	    	} else {
	    		return ef /_false;
	    	}
	    }
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * 
	 * @author kui.liu
	 *
	 */
	private class Fleiss implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (4 * ef * np - 4 * nf * ep - (nf - ep) * (nf - ep) * (nf - ep)) / ((2 * ef + nf + ep) + (2 * np + nf + ep));
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * 
	 * @author kui.liu
	 *
	 */
	private class GeometricMean implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (ef * np - nf * ep) / Math.sqrt((ef + ep) * (nf + np) * (ef + nf) * (ep + np));
		}
		
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045 
	 * 
	 * @author kui.liu
	 *
	 */
	private class Goodman implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (2 * ef - nf - ep) / (2 * ef + ep + nf);
		}
		
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Gp13 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef * (1 + 1 / (2 * ep + ef));
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Hamann implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (ef + np - ep - nf) / (ef + ep + nf + np);
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045
	 * 
	 * @author kui.liu
	 *
	 */
	private class Hamming implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef + np;
	    }
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * 
	 * @author kui.liu
	 *
	 */
	private class HarmonicMean implements Metric {
		public double value(double ef, double ep, double nf, double np) {
	    	return (ef * np - nf * ep) * ((ef + ep) * (np + nf) + (ef + nf) * (ep + np)) / ((ef + ep) * (nf + np) * (ef + nf) * (ep + np));
	    }
	}
	
	/**
	 * https://repositorium.sdum.uminho.pt/bitstream/1822/37990/1/1750.pdf
	 * 
	 * @author kui.liu
	 *
	 */
	private class Jaccard implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef / (ef + ep + nf);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Kulczynski1 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef / (nf + ep);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Kulczynski2 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	        return (ef / (ef + nf) + ef / (ef + ep)) / 2;
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class M1 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	        return (ef + np) / (nf + ep);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class M2 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	        return ef / (ef + np + 2 * (nf + ep));
	    }
	}
	
	/**
	 * https://arxiv.org/pdf/1607.04347.pdf
	 * @author kui.liu
	 *
	 */
	private class McCon implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (ef * ef - ep * nf) / ((ef + nf) * (ef + ep));
		}
	}
	
	/**
	 * https://arxiv.org/pdf/1607.04347.pdf
	 * @author kui.liu
	 *
	 */
	private class Minus implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef / (ef + nf) / (ef / (ef + nf) + ep / (ep + np)) - (1 - ef / (ef + nf)) / (1 - ef / (ef + nf) + 1 - ep / (ep + np));
		}
	}
	
	/**
	 * https://homes.cs.washington.edu/~dericp/assets/pubs/fault-localization-icse2017.pdf
	 * @author kui.liu
	 *
	 */
	private class Muse implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef - (ef + nf) / (ep + np) * ep;
		}
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * @author kui.liu
	 *
	 */
	private class Naish1 implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			if (ef == 0)
				return np;
			return -1;
		}
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Naish2 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef - ep / (ep + np + 1);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Ochiai implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return ef / Math.sqrt((ef + ep) * (ef + nf));
	    }
	}
	
	/**
	 * http://www.scielo.br/scielo.php?pid=S1415-47572004000100014&script=sci_arttext&tlng=es
	 * 
	 * @author kui.liu
	 *
	 */
	private class Ochiai2 implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef * np / Math.sqrt((ef + ep) * (nf + np) * (ef + np) * (ep + nf));
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045
	 * 
	 * @author kui.liu
	 *
	 */
	private class Overlap implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	if (Double.valueOf(ef).compareTo(0d) == 0) return 0d;
	    	if (Double.valueOf(ep).compareTo(0d) == 0
	    			|| Double.valueOf(nf).compareTo(0d) == 0) return 1d;
	    	return  ef / Math.min(Math.min(ef, ep), nf);
	    }
	}
	
	/**
	 * https://repositorium.sdum.uminho.pt/bitstream/1822/37990/1/1750.pdf
	 * @author kui.liu
	 *
	 */
	private class Qe implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return  ef / (ef + ep);
	    }
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045
	 * 
	 * @author kui.liu
	 *
	 */
	private class RogersTanimoto implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return (ef + np) / (ef + np + 2 * (nf + ep));
	    }
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * @author kui.liu
	 *
	 */
	private class Rogot1 implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (ef / (2 * ef + nf + ep) + np / (2 * np + nf + ep)) / 2;
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * @author kui.liu
	 *
	 */
	private class Rogot2 implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (ef / (ef + ep) + ef / (ef + nf) + np / (np + ep) +  np / (np + nf)) / 4;
		}
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * @author kui.liu
	 *
	 */
	private class RussellRao implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef / (ef + ep + nf + np);
		}
	}
	
	/**
	 * https://www-sciencedirect-com.proxy.bnl.lu/science/article/pii/S0164121211000045#bib0080
	 * 
	 * @author kui.liu
	 *
	 */
	private class Scott implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return (4 * ef * np - 4 * nf * ep - (nf - ep) * (nf - ep) * (nf - ep)) / ((2 * ef + nf + ep) * (2 * np + nf + ep));
		}
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class SimpleMatching implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return (ef + np) / (ef + nf + ep + np);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * 
	 * @author kui.liu
	 *
	 */
	private class Sokal implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	return 2 * (ef + np) / ( 2 * (ef + np) + nf + ep);
	    }
	}
	
	/**
	 * https://hal.inria.fr/hal-01018935/document
	 * SφrensenDice
	 * 
	 * @author kui.liu
	 *
	 */
	private class SorensenDice implements Metric {
		public double value(double ef, double ep, double nf, double np) {
	        return 2 * ef / (2 * ef + ep + nf);
	    }
	}
	
	/**
	 * http://laser.cs.umass.edu/courses/cs521-621.Fall10/documents/p273-jones.pdf
	 * 
	 * @author kui.liu
	 *
	 */
	private class Tarantula implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	Double totalFailed = Double.valueOf(ef + nf);
	    	if (totalFailed.compareTo(0d) == 0) {
				return 0;
			}
			return (ef / totalFailed) / ((ef / totalFailed) + (ep / (ep + np)));
	    }
	}
	
	/**
	 * https://ieeexplore-ieee-org.proxy.bnl.lu/abstract/document/4291037/
	 * 
	 * @author kui.liu
	 *
	 */
	private class Wong1 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	        return ef;
	    }
	}
	
	/**
	 * https://ieeexplore-ieee-org.proxy.bnl.lu/abstract/document/4291037/
	 * 
	 * @author kui.liu
	 *
	 */
	private class Wong2 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	        return  ef - ep;
	    }
	}
	
	/**
	 * https://ieeexplore-ieee-org.proxy.bnl.lu/abstract/document/4291037/
	 * 
	 * @author kui.liu
	 *
	 */
	private class Wong3 implements Metric {
	    public double value(double ef, double ep, double nf, double np) {
	    	double h;
	    	if (ep <= 2) {
	    		h = ep;
	    	} else if (ep <= 10) {
	    		h = 2 + 0.1 *(ep - 2);
	    	} else {
	    		h = 2.8 + 0.01 *(ep -10);
	    	}
	    	return ef - h;
	    }
	}
	
	/**
	 * https://dl-acm-org.proxy.bnl.lu/citation.cfm?id=1747577
	 * 
	 * @author kui.liu
	 *
	 */
	private class Zoltar implements Metric {
		public double value(double ef, double ep, double nf, double np) {
			return ef / (ef + nf + ep + (10000 * nf * ep) /ef);
		}
	}

}
