package metro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinBinaryHeap<T> {

    private final List<T> heap = new ArrayList<>();
    private final Comparator<T> comparator;

    public MinBinaryHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void add(T element) {

        heap.add(element);
        siftUp(heap.size() - 1);
    }

    private void swap(int i, int j) {

        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    private void siftUp(int index) {

        while (index > 0 && comparator.compare(heap.get(index), heap.get(parent(index))) < 0) {
            int parentIndex = parent(index);
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    public T poll() {

        if (heap.isEmpty()) {
            return null;
        }

        swap(0, heap.size() - 1);
        T result = heap.removeLast();
        siftDown();
        return result;
    }

    private void siftDown() {

        int index = 0;

        while (leftChild(index) < heap.size()) {

            int childIndex = leftChild(index);

            if (rightChild(index) < heap.size() && comparator.compare(heap.get(rightChild(index)), heap.get(leftChild(index))) < 0) {
                childIndex = rightChild(index);
            }

            if (comparator.compare(heap.get(index), heap.get(childIndex)) > 0) {
                swap(index, childIndex);
                index = childIndex;
            } else {
                return;
            }
        }
    }

    public boolean isEmpty() {

        return heap.isEmpty();
    }

    private int parent(int index) {

        return (index - 1) / 2;
    }

    private int leftChild(int index) {

        return 2 * index + 1;
    }

    private int rightChild(int index) {

        return 2 * index + 2;
    }

    public T peek() {
        if (!heap.isEmpty()) {
            return heap.get(0);
        }
        return null;
    }

    public int size() {
        return heap.size();
    }
}
