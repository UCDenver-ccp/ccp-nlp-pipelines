/**
 * 
 */
package edu.ucdenver.ccp.nlp.pipelines.evaluation.craft.conceptmapper;

/*
 * #%L
 * Colorado Computational Pharmacology's NLP pipelines
 * 							module
 * %%
 * Copyright (C) 2014 - 2017 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil.FileSuffixEnforcement;
import edu.ucdenver.ccp.common.file.FileWriterUtil.WriteMode;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.common.string.StringUtil;

/**
 * Given a directory containing output files from Concept Mapper evals, this class compiles the
 * results into a single file.
 * 
 * @author Colorado Computational Pharmacology, UC Denver; ccpsupport@ucdenver.edu
 * 
 */
public class ConceptMapperEvalOutputCompiler {

	private static final Logger logger = Logger.getLogger(ConceptMapperEvalOutputCompiler.class);

	private static final String SS_CONTIG_MATCH = "SearchStrategy:CONTIGUOUS_MATCH";
	private static final String SS_SKIP_ANY_MATCH = "SearchStrategy:SKIP_ANY_MATCH";
	private static final String SS_SKIP_ANY_ALLOW_OVERLAP = "SearchStrategy:SKIP_ANY_MATCH_ALLOW_OVERLAP";
	private static final String CM_FOLD_DIGITS = "CaseMatch:CASE_FOLD_DIGITS";
	private static final String CM_IGNORE = "CaseMatch:CASE_IGNORE";
	private static final String CM_SENSITIVE = "CaseMatch:CASE_SENSITIVE";
	private static final String CM_INSENSITIVE = "CaseMatch:CASE_INSENSITIVE";
	private static final String STEMMER_PORTER = "Stemmer:PORTER";
	private static final String STEMMER_NONE = "Stemmer:NONE";
	private static final String STEMMER_BIOLEMMATIZER = "Stemmer:BIOLEMMATIZER";
	private static final String STOPWORDS_NONE = "Stopwords:NONE";
	private static final String STOPWORDS_PUBMED = "Stopwords:PUBMED";
	private static final String ORDER_IND_LOOKUP_ON = "OrderIndependentLookup:ON";
	private static final String ORDER_IND_LOOKUP_OFF = "OrderIndependentLookup:OFF";
	private static final String FIND_ALL_MATCHES_YES = "FindAllMatches:YES";
	private static final String FIND_ALL_MATCHES_NO = "FindAllMatches:NO";
	private static final String SYNONYM_TYPE_EXACT_ONLY = "SynonymType:EXACT_ONLY";
	private static final String SYNONYM_TYPE_ALL = "SynonymType:ALL";
	
//	private static final String REPLACE_COMMA_ON = "ReplaceCommaWithAnd:ON";
//	private static final String REPLACE_COMMA_OFF = "ReplaceCommaWithAnd:OFF";

	private JFreeChart stemmerComparisonChart;
	private XYPlot stemmerComparisonPlot;
	private JFreeChart stopWordsComparisonChart;
	private XYPlot stopwordsComparisonPlot;
	private JFreeChart searchStrategyComparisonChart;
	private XYPlot searchStrategyComparisonPlot;
	private JFreeChart caseMatchComparisonChart;
	private XYPlot caseMatchComparisonPlot;
	private JFreeChart orderIndependentLookupComparisonChart;
	private XYPlot orderIndependentLookupComparisonPlot;
	private JFreeChart findAllMatchesComparisonChart;
	private XYPlot findAllMatchesComparisonPlot;
	private JFreeChart synonymTypeComparisonChart;
	private XYPlot synonymTypeComparisonPlot;

	private Map<String, XYSeries> paramValueKeyToSeriesMap;

	public void compileOutput(File evalFileDirectory, File outputFilePrefix) 
	throws IOException, DataFormatException {
		BufferedWriter writer = null;
		try {
			initializeCharts();
			File outputFile = new File(outputFilePrefix.getAbsolutePath() + ".utf8");
			logger.info("Writing file: " + outputFile.getAbsolutePath());
			writer = FileWriterUtil.initBufferedWriter(outputFile, CharacterEncoding.UTF_8, WriteMode.OVERWRITE,
					FileSuffixEnforcement.OFF);

			for (File file : evalFileDirectory.listFiles()) {
				if (StringUtil.endsWithRegex(file.getName(), "\\.\\d+")) {
					logger.info("Extracting evaluation results from: " + file.getAbsolutePath());
					List<String> lastTwoLines = getLastTwoLines(file);
					validateLastTwoLines(lastTwoLines, file);
					addDataToCharts(lastTwoLines);
					String outLine = extractCompiledOutput(lastTwoLines, file);
					writer.write(outLine);
					writer.newLine();
				}
			}
			assignDataSeriesToCharts();
			exportCharts(outputFilePrefix);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * 
	 */
	private void assignDataSeriesToCharts() {
		assignSearchStrategyDataSeriesToChart();
		assignCaseMatchDataSeriesToChart();
		assignFindAllMatchesDataSeriesToChart();
		assignStemmerDataSeriesToChart();
		assignStopwordDataSeriesToChart();
		assignOrderIndLookupDataSeriesToChart();
		assignSynonymTypeDataSeriesToChart();
		assignSearchStrategyDataSeriesToChart();

		assignFscoreLinesToCharts();

	}

	/**
	 * 
	 */
	private void assignFscoreLinesToCharts() {
		XYDataset fLines = getFLines();
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false); // Lines only
		for (int i = 0; i < 9; i++) {
			renderer.setSeriesPaint(i, Color.LIGHT_GRAY);
		}
		assignFscoreLinesToChart(searchStrategyComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(caseMatchComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(stemmerComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(stopwordsComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(orderIndependentLookupComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(synonymTypeComparisonPlot, fLines, renderer);
		assignFscoreLinesToChart(findAllMatchesComparisonPlot, fLines, renderer);
	}

	/**
	 * @param searchStrategyComparisonPlot2
	 * @param fLines
	 * @param renderer
	 */
	private void assignFscoreLinesToChart(XYPlot plot, XYDataset fLines, XYItemRenderer renderer) {
		plot.setDataset(1, fLines);
		plot.setRenderer(1, renderer);
		plot.mapDatasetToDomainAxis(1, 0);
		plot.mapDatasetToRangeAxis(1, 0);
	}

	public static XYDataset getFLines() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(getFSeries(0.1f));
		dataset.addSeries(getFSeries(0.2f));
		dataset.addSeries(getFSeries(0.3f));
		dataset.addSeries(getFSeries(0.4f));
		dataset.addSeries(getFSeries(0.5f));
		dataset.addSeries(getFSeries(0.6f));
		dataset.addSeries(getFSeries(0.7f));
		dataset.addSeries(getFSeries(0.8f));
		dataset.addSeries(getFSeries(0.9f));
		return dataset;
	}

	private static XYSeries getFSeries(float fValue) {
		XYSeries series = new XYSeries("F=" + fValue);
		for (float r = 0; r <= 1.0; r += 0.01) {
			float p = (fValue * r) / (2 * r - fValue);
			if (p > 0 && p <= 1.0) {
				System.out.println("Adding f=" + fValue + " p=" + p + " r=" + r);
				series.add(p, r);
			}
		}
		return series;
	}

	/**
	 * 
	 */
	private void assignSearchStrategyDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(SS_CONTIG_MATCH));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(SS_SKIP_ANY_ALLOW_OVERLAP));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(SS_SKIP_ANY_MATCH));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		searchStrategyComparisonPlot.setDataset(0, dataSet);
		searchStrategyComparisonPlot.setRenderer(0, renderer);
		searchStrategyComparisonPlot.setDomainAxis(0, xAxis);
		searchStrategyComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		searchStrategyComparisonPlot.mapDatasetToDomainAxis(0, 0);
		searchStrategyComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignCaseMatchDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(CM_FOLD_DIGITS));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(CM_IGNORE));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(CM_INSENSITIVE));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(CM_SENSITIVE));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		caseMatchComparisonPlot.setDataset(0, dataSet);
		caseMatchComparisonPlot.setRenderer(0, renderer);
		caseMatchComparisonPlot.setDomainAxis(0, xAxis);
		caseMatchComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		caseMatchComparisonPlot.mapDatasetToDomainAxis(0, 0);
		caseMatchComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignStemmerDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(STEMMER_BIOLEMMATIZER));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(STEMMER_NONE));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(STEMMER_PORTER));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		stemmerComparisonPlot.setDataset(0, dataSet);
		stemmerComparisonPlot.setRenderer(0, renderer);
		stemmerComparisonPlot.setDomainAxis(0, xAxis);
		stemmerComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		stemmerComparisonPlot.mapDatasetToDomainAxis(0, 0);
		stemmerComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignStopwordDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(STOPWORDS_NONE));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(STOPWORDS_PUBMED));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		stopwordsComparisonPlot.setDataset(0, dataSet);
		stopwordsComparisonPlot.setRenderer(0, renderer);
		stopwordsComparisonPlot.setDomainAxis(0, xAxis);
		stopwordsComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		stopwordsComparisonPlot.mapDatasetToDomainAxis(0, 0);
		stopwordsComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignOrderIndLookupDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(ORDER_IND_LOOKUP_OFF));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(ORDER_IND_LOOKUP_ON));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		orderIndependentLookupComparisonPlot.setDataset(0, dataSet);
		orderIndependentLookupComparisonPlot.setRenderer(0, renderer);
		orderIndependentLookupComparisonPlot.setDomainAxis(0, xAxis);
		orderIndependentLookupComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		orderIndependentLookupComparisonPlot.mapDatasetToDomainAxis(0, 0);
		orderIndependentLookupComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignSynonymTypeDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(SYNONYM_TYPE_ALL));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(SYNONYM_TYPE_EXACT_ONLY));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		synonymTypeComparisonPlot.setDataset(0, dataSet);
		synonymTypeComparisonPlot.setRenderer(0, renderer);
		synonymTypeComparisonPlot.setDomainAxis(0, xAxis);
		synonymTypeComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		synonymTypeComparisonPlot.mapDatasetToDomainAxis(0, 0);
		synonymTypeComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * 
	 */
	private void assignFindAllMatchesDataSeriesToChart() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		dataSet.addSeries(paramValueKeyToSeriesMap.get(FIND_ALL_MATCHES_NO));
		dataSet.addSeries(paramValueKeyToSeriesMap.get(FIND_ALL_MATCHES_YES));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true); // Shapes only
		ValueAxis xAxis = new NumberAxis("Recall");
		ValueAxis yAxis = new NumberAxis("Precision");

		findAllMatchesComparisonPlot.setDataset(0, dataSet);
		findAllMatchesComparisonPlot.setRenderer(0, renderer);
		findAllMatchesComparisonPlot.setDomainAxis(0, xAxis);
		findAllMatchesComparisonPlot.setRangeAxis(0, yAxis);

		// Map the scatter to the first Domain and first Range
		findAllMatchesComparisonPlot.mapDatasetToDomainAxis(0, 0);
		findAllMatchesComparisonPlot.mapDatasetToRangeAxis(0, 0);
	}

	/**
	 * @param lastTwoLines
	 */
	private void addDataToCharts(List<String> lastTwoLines) {
		double[] pr = getPrecisionRecall(lastTwoLines);
		for (String paramValue : getParamValues(lastTwoLines)) {
			if (!paramValueKeyToSeriesMap.containsKey(paramValue)) {
				throw new RuntimeException("Missing series for: " + paramValue);
			}
			paramValueKeyToSeriesMap.get(paramValue).add(pr[1], pr[0]);
		}
	}

	/**
	 * @param lastTwoLines
	 * @return
	 */
	private List<String> getParamValues(List<String> lastTwoLines) {
		String lastLine = lastTwoLines.get(1);
		lastLine = StringUtil.removePrefix(lastLine, "[");
		lastLine = StringUtil.removeSuffix(lastLine, "]");
		return Arrays.asList(lastLine.split(", "));
	}

	/**
	 * @param lastTwoLines
	 * @return
	 */
	private double[] getPrecisionRecall(List<String> lastTwoLines) {
		String penultimateLine = lastTwoLines.get(0);
		String[] toks = penultimateLine.split("\\t");
		double tp = Double.parseDouble(toks[1]);
		double fp = Double.parseDouble(toks[2]);
		double fn = Double.parseDouble(toks[3]);
		return new double[] { (tp / (tp + fp)), (tp / (tp + fn)) };
	}

	/**
	 * @throws IOException
	 * 
	 */
	private void exportCharts(File outputFilePrefix) throws IOException {
		File stemmerComparisonChartFile = new File(outputFilePrefix.getAbsolutePath() + "-stemmer-comparison.png");
		File stopWordsComparisonChartFile = new File(outputFilePrefix.getAbsolutePath() + "-stopwords-comparison.png");
		File searchStrategyComparisonChartFile = new File(outputFilePrefix.getAbsolutePath()
				+ "-search-strategy-comparison.png");
		File caseMatchComparisonChartFile = new File(outputFilePrefix.getAbsolutePath() + "-case-match-comparison.png");
		File orderIndependentLookupComparisonChartFile = new File(outputFilePrefix.getAbsolutePath()
				+ "-order-ind-lookup-comparison.png");
		File findAllMatchesComparisonChartFile = new File(outputFilePrefix.getAbsolutePath()
				+ "-find-all-matches-comparison.png");
		File synonymTypeComparisonChartFile = new File(outputFilePrefix.getAbsolutePath()
				+ "-synonym-type-comparison.png");

		logger.info("Saving chart: " + stemmerComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(stemmerComparisonChartFile, stemmerComparisonChart, 500, 500);
		logger.info("Saving chart: " + stopWordsComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(stopWordsComparisonChartFile, stopWordsComparisonChart, 500, 500);
		logger.info("Saving chart: " + searchStrategyComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(searchStrategyComparisonChartFile, searchStrategyComparisonChart, 500, 500);
		logger.info("Saving chart: " + caseMatchComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(caseMatchComparisonChartFile, caseMatchComparisonChart, 500, 500);
		logger.info("Saving chart: " + orderIndependentLookupComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(orderIndependentLookupComparisonChartFile, orderIndependentLookupComparisonChart,
				500, 500);
		logger.info("Saving chart: " + findAllMatchesComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(findAllMatchesComparisonChartFile, findAllMatchesComparisonChart, 500, 500);
		logger.info("Saving chart: " + synonymTypeComparisonChartFile.getAbsolutePath());
		ChartUtilities.saveChartAsPNG(synonymTypeComparisonChartFile, synonymTypeComparisonChart, 500,
				500);
	}

	/**
	 * 
	 */
	private void initializeCharts() {
		stemmerComparisonPlot = new XYPlot();
		stemmerComparisonChart = new JFreeChart("Stemmer Comparison", JFreeChart.DEFAULT_TITLE_FONT,
				stemmerComparisonPlot, true);

		stopwordsComparisonPlot = new XYPlot();
		stopWordsComparisonChart = new JFreeChart("Stopwords Comparison", JFreeChart.DEFAULT_TITLE_FONT,
				stopwordsComparisonPlot, true);

		searchStrategyComparisonPlot = new XYPlot();
		searchStrategyComparisonChart = new JFreeChart("Search Strategy Comparison", JFreeChart.DEFAULT_TITLE_FONT,
				searchStrategyComparisonPlot, true);

		caseMatchComparisonPlot = new XYPlot();
		caseMatchComparisonChart = new JFreeChart("Case Match Comparison", JFreeChart.DEFAULT_TITLE_FONT,
				caseMatchComparisonPlot, true);

		orderIndependentLookupComparisonPlot = new XYPlot();
		orderIndependentLookupComparisonChart = new JFreeChart("Order-Independent-Lookup Comparison",
				JFreeChart.DEFAULT_TITLE_FONT, orderIndependentLookupComparisonPlot, true);

		findAllMatchesComparisonPlot = new XYPlot();
		findAllMatchesComparisonChart = new JFreeChart("Find-All-Matches Comparison", JFreeChart.DEFAULT_TITLE_FONT,
				findAllMatchesComparisonPlot, true);

		synonymTypeComparisonPlot = new XYPlot();
		synonymTypeComparisonChart = new JFreeChart("Synonym Type Comparison",
				JFreeChart.DEFAULT_TITLE_FONT, synonymTypeComparisonPlot, true);

		paramValueKeyToSeriesMap = new HashMap<String, XYSeries>();
		paramValueKeyToSeriesMap.put(SS_CONTIG_MATCH, new XYSeries(SS_CONTIG_MATCH));
		paramValueKeyToSeriesMap.put(SS_SKIP_ANY_MATCH, new XYSeries(SS_SKIP_ANY_MATCH));
		paramValueKeyToSeriesMap.put(SS_SKIP_ANY_ALLOW_OVERLAP, new XYSeries(SS_SKIP_ANY_ALLOW_OVERLAP));
		paramValueKeyToSeriesMap.put(CM_FOLD_DIGITS, new XYSeries(CM_FOLD_DIGITS));
		paramValueKeyToSeriesMap.put(CM_IGNORE, new XYSeries(CM_IGNORE));
		paramValueKeyToSeriesMap.put(CM_INSENSITIVE, new XYSeries(CM_INSENSITIVE));
		paramValueKeyToSeriesMap.put(CM_SENSITIVE, new XYSeries(CM_SENSITIVE));
		paramValueKeyToSeriesMap.put(STEMMER_NONE, new XYSeries(STEMMER_NONE));
		paramValueKeyToSeriesMap.put(STEMMER_PORTER, new XYSeries(STEMMER_PORTER));
		paramValueKeyToSeriesMap.put(STEMMER_BIOLEMMATIZER, new XYSeries(STEMMER_BIOLEMMATIZER));
		paramValueKeyToSeriesMap.put(STOPWORDS_NONE, new XYSeries(STOPWORDS_NONE));
		paramValueKeyToSeriesMap.put(STOPWORDS_PUBMED, new XYSeries(STOPWORDS_PUBMED));
		paramValueKeyToSeriesMap.put(ORDER_IND_LOOKUP_OFF, new XYSeries(ORDER_IND_LOOKUP_OFF));
		paramValueKeyToSeriesMap.put(ORDER_IND_LOOKUP_ON, new XYSeries(ORDER_IND_LOOKUP_ON));
		paramValueKeyToSeriesMap.put(SYNONYM_TYPE_ALL, new XYSeries(SYNONYM_TYPE_ALL));
		paramValueKeyToSeriesMap.put(SYNONYM_TYPE_EXACT_ONLY, new XYSeries(SYNONYM_TYPE_EXACT_ONLY));
		paramValueKeyToSeriesMap.put(FIND_ALL_MATCHES_NO, new XYSeries(FIND_ALL_MATCHES_NO));
		paramValueKeyToSeriesMap.put(FIND_ALL_MATCHES_YES, new XYSeries(FIND_ALL_MATCHES_YES));

	}

	/**
	 * looks at the last two lines of the input file. The final line contains the concept mapper
	 * parameters while the penultimate line contains the evaluation performance scores (P/R/F)
	 * 
	 * <pre>
	 * Evaluation Set  4358    3547    1415    P=0.5512966476913346    R=0.7548934695998615    F=0.6372276648632841
	 * [SearchStrategy:SKIP_ANY_MATCH, CaseMatch:CASE_FOLD_DIGITS, Stemmer:BIOLEMMATIZER, Stopwords:PUBMED, OrderIndependentLookup:OFF, FindAllMatches:YES, ReplaceCommaWithAnd:OFF]
	 * 
	 * </pre>
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static String extractCompiledOutput(List<String> lastTwoLines, File file) throws IOException {
		String paramIndex = file.getName().substring(file.getName().lastIndexOf('.') + 1);
		String penultimateLine = lastTwoLines.get(0);
		String lastLine = lastTwoLines.get(1);
		String[] toks = penultimateLine.split("\\t");
		String tp = toks[1];
		String fp = toks[2];
		String fn = toks[3];

		lastLine = StringUtil.removePrefix(lastLine, "[");
		lastLine = StringUtil.removeSuffix(lastLine, "]");

		String outStr = paramIndex + "\t" + tp + "\t" + fp + "\t" + fn;
		for (String tok : lastLine.split(", ")) {
			outStr += ("\t" + tok);
		}
		return outStr;
	}

	/**
	 * @param file
	 * @return List of two strings: the last two lines
	 * @throws IOException
	 */
	private static List<String> getLastTwoLines(File file) throws IOException {
		String lastLine = null;
		String penultimateLine = null;
		StreamLineIterator lineIter = null;
		for (lineIter = new StreamLineIterator(file, CharacterEncoding.UTF_8); lineIter.hasNext();) {
			penultimateLine = lastLine;
			lastLine = lineIter.next().getText();
		}
		lineIter.close();
		
		return CollectionsUtil.createList(penultimateLine, lastLine);
	}

	private static void validateLastTwoLines(List<String> lines, File file) 
	throws DataFormatException{
		//Evaluation Set  4362    2528    1411    P=0.6330914368650218    R=0.7555863502511693    F=0.6889362710258232
		//[SearchStrategy:CONTIGUOUS_MATCH, CaseMatch:CASE_INSENSITIVE, Stemmer:BIOLEMMATIZER, Stopwords:PUBMED, OrderIndependentLookup:ON, FindAl

		if (! (lines.get(0).startsWith("Evaluation Set")
				&& lines.get(1).contains("SearchStrategy:")) ) {
			throw new DataFormatException("invalid last two lines in file: " + file.getAbsolutePath() 
				+ " \"" + lines.get(0) + "\", "
				+ " \"" + lines.get(1) + "\"");
		}
		
	}

	/**
	 * @param args
	 *            args[0] = the directory containing the eval output files<br>
	 *            args[1] = the file created by this program that compiles all of the output into
	 *            one place
	 */
	public static void main(String[] args) {
		System.out.println("running ConceptMapperEvalOutputCompiler ");
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		File evalFileDirectory = new File(args[0]);
		File outputFile = new File(args[1]);

		try {
			new ConceptMapperEvalOutputCompiler().compileOutput(evalFileDirectory, outputFile);
		}
		catch (IOException e) {
			System.err.println("error:" + e);
			e.printStackTrace();
			System.exit(-1);
		}
		catch (DataFormatException e) {
			System.err.println("error:" + e);
			e.printStackTrace();
			System.exit(-2);
		}
	}
}
