package edu.ucdenver.ccp.nlp.pipelines.runner;

public enum PipelineKey {
	XML2TXT(0),
	CONCEPTMAPPER(1);

	/*
	 * Priority provides a simple way to order pipeline runs. Integer values
	 * increase with lower priority, so pipelines with 0 will be run prior to
	 * all other pipelines.
	 */
	private int priority;

	private PipelineKey(int priority) {
		this.priority = priority;
	}

	public int priority() {
		return this.priority;
	}
}
