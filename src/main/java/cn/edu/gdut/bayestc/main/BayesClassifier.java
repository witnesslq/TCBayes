package cn.edu.gdut.bayestc.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import cn.edu.gdut.bayestc.classword.ChineseSpliter;
import cn.edu.gdut.bayestc.textclassifier.entity.ClassifyResult;
import cn.edu.gdut.bayestc.util.ClassConditionalProbability;
import cn.edu.gdut.bayestc.util.ClassPorpertiesUtils;
import cn.edu.gdut.bayestc.util.PriorProbability;
import cn.edu.gdut.bayestc.util.StopWordsHandler;
import cn.edu.gdut.bayestc.util.TrainingSetManager;

/**
 * 朴素贝叶斯分类器
 */
public class BayesClassifier {

	private static final String DEFAULT_TEXT = "微软公司提出以446亿美元的价格收购雅虎中国网2月1日报道 美联社消息，微软公司提出以446亿美元现金加股票的价格收购搜索网站雅虎公司。微软提出以每股31美元的价格收购雅虎。微软的收购报价较雅虎1月31日的收盘价19.18美元溢价62%。微软公司称雅虎公司的股东可以选择以现金或股票进行交易。微软和雅虎公司在2006年底和2007年初已在寻求双方合作。而近两年，雅虎一直处于困境：市场份额下滑、运营业绩不佳、股价大幅下跌。对于力图在互联网市场有所作为的微软来说，收购雅虎无疑是一条捷径，因为双方具有非常强的互补性。(小桥)";
	private TrainingSetManager tdm;// 训练集管理器
	// private String trainnigDataPath;//训练集路径
	private static double zoomFactor = 14.0f;

	/**
	 * 默认的构造器，初始化训练集
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public BayesClassifier() throws UnsupportedEncodingException {
		tdm = new TrainingSetManager();

	}

	/**
	 * 计算给定的文本属性向量X在给定的分类Cj中的类条件概率 <code>ClassConditionalProbability</code>
	 * 连乘值
	 * 
	 * @param words
	 *                给定的文本属性向量
	 * @param aClass
	 *                给定的类别
	 * @return 分类条件概率连乘值，即<br>
	 */
	double calcProd(String[] words, String aClass) {
		double ret = 1.0F;
		// 类条件概率连乘
		for (int i = 0; i < words.length; i++) {
			String wordi = words[i];
			// 因为结果过小，因此在连乘之前放大14倍，这对最终结果并无影响，因为我们只是比较概率大小而已
			ret *= ClassConditionalProbability.calculatePxc(wordi, aClass)
					* zoomFactor;
		}
		System.out.println(ret);
		// 再乘以先验概率
		ret *= PriorProbability.calculatePc(aClass);
		System.out.println(ret);
		return ret;
	}

	/**
	 * 去掉停用词
	 * 
	 * @param text
	 *                给定的文本
	 * @return 去停用词后结果
	 */
	public String[] DropStopWords(String[] oldWords) {
		Vector<String> v1 = new Vector<String>();
		for (int i = 0; i < oldWords.length; ++i) {
			if (StopWordsHandler.IsStopWord(oldWords[i]) == false) {// 不是停用词
				v1.add(oldWords[i]);
			}
		}
		String[] newWords = new String[v1.size()];
		System.out.println(v1.size());
		v1.toArray(newWords);		

//		String[] newWords = oldWords;
		

		if(newWords.length<oldWords.length-7){
			System.out.println(oldWords.length);
			System.out.println(newWords.length);
			System.out.println("好多停用词");
		}
		return newWords;
	}

	/**
	 * 对给定的文本进行分类
	 * 
	 * @param text
	 *                给定的文本
	 * @return 分类结果
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public String classify(String text) throws IOException {
		String[] terms = null;
		terms = ChineseSpliter.split(text, " ").split(" ");// 中文分词处理(分词后结果可能还包含有停用词）
//		terms = DropStopWords(terms);//去掉停用词，以免影响分类 
		String[] Classes = tdm.getTraningClassifications();// 分类
		double probility = 0.0F;
		List<ClassifyResult> crs = new ArrayList<ClassifyResult>();// 分类结果
		for (int i = 0; i < Classes.length; i++) {
			String Ci = Classes[i];// 第i个分类
			probility = calcProd(terms, Ci);// 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
			// 保存分类结果
			ClassifyResult cr = new ClassifyResult();
			cr.classification = Ci;// 分类
			cr.probility = probility;// 关键字在分类的条件概率
			System.out.println("In process....");
			System.out.println(ClassPorpertiesUtils.classProperties
					.get(Ci) + "：" + probility);
			crs.add(cr);
		}
		// 对最后概率结果进行排序
		java.util.Collections.sort(crs, new Comparator() {
			public int compare(final Object o1, final Object o2) {
				final ClassifyResult m1 = (ClassifyResult) o1;
				final ClassifyResult m2 = (ClassifyResult) o2;
				final double ret = m1.probility - m2.probility;
				if (ret < 0) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		// 返回概率最大的分类
		return crs.get(0).classification;
	}

	public static void main(String[] args) throws IOException {
		long time = System.currentTimeMillis();
		System.out.println();
		String text = BayesClassifier.DEFAULT_TEXT;
		BayesClassifier classifier = new BayesClassifier();// 构造Bayes分类器
		String result = classifier.classify(text);// 进行分类
		System.out.println("此项属于["
				+ ClassPorpertiesUtils.classProperties
						.getProperty(result) + "]");
		System.out.println((System.currentTimeMillis()-time)/1000);
	}
}