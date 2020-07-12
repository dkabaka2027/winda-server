package co.winda.predictive.data

import org.deeplearning4j.datasets.iterator.BaseDatasetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher

class WindaDataSetIterator(batch: Int, numExamples: Int, fetcher: WindaDataFetcher)
  extends BaseDatasetIterator(batch, numExamples, fetcher) {}