package org.cardanofoundation.lob.app.support.utils_support;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@UtilityClass
public class MorePartition {

    public <T> Iterable<Partition<T>> partition(Iterable<T> it, int size) {
        return toPartitionList(Iterables.partition(it, size));
    }

    private <T> List<Partition<T>> toPartitionList(Iterable<? extends Iterable<T>> iterable) {
        val partitionList = new ArrayList<Partition<T>>();

        int partitionIndex = 0;
        for (val partition : iterable) {
            partitionIndex++;
            partitionList.add(new Partition<>(partition, partitionIndex, Iterables.size(iterable)));
        }

        return partitionList;
    }

    @AllArgsConstructor
    public static class Partition<T> {

        @Getter
        private final Iterable<T> elements;
        private final int partitionIndex;
        private final int totalPartitions;

        public Set<T> asSet() {
            return Sets.newHashSet(elements);
        }

        public boolean isFirst() {
            return partitionIndex == 1;
        }

        public boolean isLast() {
            return partitionIndex == totalPartitions;
        }
    }

}
