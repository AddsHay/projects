package edu.berkeley.cs186.database.concurrency;

import edu.berkeley.cs186.database.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LockContext wraps around LockManager to provide the hierarchical structure
 * of multigranularity locking. Calls to acquire/release/etc. locks should
 * be mostly done through a LockContext, which provides access to locking
 * methods at a certain point in the hierarchy (database, table X, etc.)
 */
public class LockContext {
    // You should not remove any of these fields. You may add additional
    // fields/methods as you see fit.

    // The underlying lock manager.
    protected final LockManager lockman;

    // The parent LockContext object, or null if this LockContext is at the top of the hierarchy.
    protected final LockContext parent;

    // The name of the resource this LockContext represents.
    protected ResourceName name;

    // Whether this LockContext is readonly. If a LockContext is readonly, acquire/release/promote/escalate should
    // throw an UnsupportedOperationException.
    protected boolean readonly;

    // A mapping between transaction numbers, and the number of locks on children of this LockContext
    // that the transaction holds.
    protected final Map<Long, Integer> numChildLocks;

    // You should not modify or use this directly.
    protected final Map<String, LockContext> children;

    // Whether or not any new child LockContexts should be marked readonly.
    protected boolean childLocksDisabled;

    public LockContext(LockManager lockman, LockContext parent, String name) {
        this(lockman, parent, name, false);
    }

    protected LockContext(LockManager lockman, LockContext parent, String name,
                          boolean readonly) {
        this.lockman = lockman;
        this.parent = parent;
        if (parent == null) {
            this.name = new ResourceName(name);
        } else {
            this.name = new ResourceName(parent.getResourceName(), name);
        }
        this.readonly = readonly;
        this.numChildLocks = new ConcurrentHashMap<>();
        this.children = new ConcurrentHashMap<>();
        this.childLocksDisabled = readonly;
    }

    /**
     * Gets a lock context corresponding to `name` from a lock manager.
     */
    public static LockContext fromResourceName(LockManager lockman, ResourceName name) {
        Iterator<String> names = name.getNames().iterator();
        LockContext ctx;
        String n1 = names.next();
        ctx = lockman.context(n1);
        while (names.hasNext()) {
            String n = names.next();
            ctx = ctx.childContext(n);
        }
        return ctx;
    }

    /**
     * Get the name of the resource that this lock context pertains to.
     */
    public ResourceName getResourceName() {
        return name;
    }

    /**
     * Acquire a `lockType` lock, for transaction `transaction`.
     * <p>
     * Note: you must make any necessary updates to numChildLocks, or else calls
     * to LockContext#getNumChildren will not work properly.
     *
     * @throws InvalidLockException          if the request is invalid
     * @throws DuplicateLockRequestException if a lock is already held by the
     *                                       transaction.
     * @throws UnsupportedOperationException if context is readonly
     */
    public void acquire(TransactionContext transaction, LockType lockType)
            throws InvalidLockException, DuplicateLockRequestException {
        // TODO(proj4_part2): implement
        if (readonly) {
            throw new UnsupportedOperationException("read only not allowed");
        } else if (!lockman.getLockType(transaction, name).equals(LockType.NL)) {
            throw new DuplicateLockRequestException("Dup lock");
        }
        boolean bool = false;
        if (parent != null) {

            List<Lock> locks = parentContext().lockman.getLocks(transaction);
            for (Lock lock : locks) {
                if (!lock.name.isDescendantOf(name) && LockType.canBeParentLock(lock.lockType, lockType)) {
                    bool = true;
                    break;
                }
            }
            LockType parentLockType = parent.getExplicitLockType(transaction);
            if (parentLockType.equals(LockType.NL)) {
                bool = true;
            }
        }
        if (parent == null || bool) {
            lockman.acquire(transaction, name, lockType);
            if (parent != null) {
                if (parent.numChildLocks.containsKey(transaction.getTransNum())) {
                    int count = parent.numChildLocks.get(transaction.getTransNum());
                    parent.numChildLocks.put(transaction.getTransNum(), count + 1);
                } else {
                    parent.numChildLocks.put(transaction.getTransNum(), 1);
                }
            }
        } else {
            throw new InvalidLockException("Acquisition is not allowed");
        }
    }

    /**
     * Release `transaction`'s lock on `name`.
     * <p>
     * Note: you *must* make any necessary updates to numChildLocks, or
     * else calls to LockContext#getNumChildren will not work properly.
     *
     * @throws NoLockHeldException           if no lock on `name` is held by `transaction`
     * @throws InvalidLockException          if the lock cannot be released because
     *                                       doing so would violate multigranularity locking constraints
     * @throws UnsupportedOperationException if context is readonly
     */
    public void release(TransactionContext transaction)
            throws NoLockHeldException, InvalidLockException {
        // TODO(proj4_part2): implement
        if (readonly) {
            throw new UnsupportedOperationException("read only not allowed");
        } else if (numChildLocks.containsKey(transaction.getTransNum()) && numChildLocks.get(transaction.getTransNum()) > 0) {
            throw new InvalidLockException("Invalid Lock");
        } else if (lockman.getLockType(transaction, name).equals(LockType.NL)) {
            throw new NoLockHeldException("No Lock Held");
        }
        lockman.release(transaction, name);
        if (parent != null) {
            if (parent.numChildLocks.containsKey(transaction.getTransNum())) {
                int count = parent.numChildLocks.get(transaction.getTransNum());
                parent.numChildLocks.put(transaction.getTransNum(), count - 1);
            }
        }
    }

    /**
     * Promote `transaction`'s lock to `newLockType`. For promotion to SIX from
     * IS/IX, all S and IS locks on descendants must be simultaneously
     * released. The helper function sisDescendants may be helpful here.
     * <p>
     * Note: you *must* make any necessary updates to numChildLocks, or else
     * calls to LockContext#getNumChildren will not work properly.
     *
     * @throws DuplicateLockRequestException if `transaction` already has a
     *                                       `newLockType` lock
     * @throws NoLockHeldException           if `transaction` has no lock
     * @throws InvalidLockException          if the requested lock type is not a
     *                                       promotion or promoting would cause the lock manager to enter an invalid
     *                                       state (e.g. IS(parent), X(child)). A promotion from lock type A to lock
     *                                       type B is valid if B is substitutable for A and B is not equal to A, or
     *                                       if B is SIX and A is IS/IX/S, and invalid otherwise. hasSIXAncestor may
     *                                       be helpful here.
     * @throws UnsupportedOperationException if context is readonly
     */
    public void promote(TransactionContext transaction, LockType newLockType)
            throws DuplicateLockRequestException, NoLockHeldException, InvalidLockException {
        // TODO(proj4_part2): implement
        LockType currLockType = getExplicitLockType(transaction);
        if (readonly) {
            throw new UnsupportedOperationException("Unsupported Operation");
        } else if (currLockType.equals(LockType.NL)) {
            throw new NoLockHeldException("No Lock Held");
        } else if (currLockType.equals(newLockType)) {
            throw new DuplicateLockRequestException("Duplicate Lock");
        } else if (LockType.substitutable(newLockType, currLockType) || (newLockType.equals(LockType.SIX) && (currLockType.equals(LockType.IS)
                || currLockType.equals(LockType.IX) || currLockType.equals(LockType.S)))) {
            if (newLockType.equals(LockType.SIX) && (currLockType.equals(LockType.IX) || currLockType.equals(LockType.IS))) {
                List<ResourceName> releaseNames = new ArrayList<>();
                releaseNames.add(name);
                releaseNames.addAll(sisDescendants(transaction));
                lockman.acquireAndRelease(transaction, name, newLockType, releaseNames);
            } else {
                lockman.promote(transaction, name, newLockType);
            }
        }
    }

    /**
     * Escalate `transaction`'s lock from descendants of this context to this
     * level, using either an S or X lock. There should be no descendant locks
     * after this call, and every operation valid on descendants of this context
     * before this call must still be valid. You should only make *one* mutating
     * call to the lock manager, and should only request information about
     * TRANSACTION from the lock manager.
     * <p>
     * For example, if a transaction has the following locks:
     * <p>
     * IX(database)
     * /         \
     * IX(table1)    S(table2)
     * /      \
     * S(table1 page3)  X(table1 page5)
     * <p>
     * then after table1Context.escalate(transaction) is called, we should have:
     * <p>
     * IX(database)
     * /         \
     * X(table1)     S(table2)
     * <p>
     * You should not make any mutating calls if the locks held by the
     * transaction do not change (such as when you call escalate multiple times
     * in a row).
     * <p>
     * Note: you *must* make any necessary updates to numChildLocks of all
     * relevant contexts, or else calls to LockContext#getNumChildren will not
     * work properly.
     *
     * @throws NoLockHeldException           if `transaction` has no lock at this level
     * @throws UnsupportedOperationException if context is readonly
     */
    public void escalate(TransactionContext transaction) throws NoLockHeldException {
        // TODO(proj4_part2): implement
        if (readonly) {
            throw new UnsupportedOperationException("read only.");
        } else if (getExplicitLockType(transaction) == LockType.NL) {
            throw new NoLockHeldException("no lock.");
        } else if (getExplicitLockType(transaction) == LockType.S || getExplicitLockType(transaction) == LockType.X) {
            return;
        }
        LockType currLockType = getExplicitLockType(transaction);
        List<ResourceName> releaseNames = new ArrayList<>();
        releaseNames.add(name);
        List<Lock> descendants = new ArrayList<>();
        for (Lock lock : lockman.getLocks(transaction)) {
            if (lock.name.isDescendantOf(this.name)) {
                releaseNames.add(lock.name);
                descendants.add(lock);
            }
        }

        LockType newLockType = LockType.NL;
        if (currLockType.equals(LockType.IS)) {
            newLockType = LockType.S;
        } else if (currLockType.equals(LockType.IX) || currLockType.equals(LockType.SIX)) {
            newLockType = LockType.X;
        }


        lockman.acquireAndRelease(transaction, name, newLockType, releaseNames);
        numChildLocks.put(transaction.getTransNum(), 0);

    }

    /**
     * Get the type of lock that `transaction` holds at this level, or NL if no
     * lock is held at this level.
     */
    public LockType getExplicitLockType(TransactionContext transaction) {
        return lockman.getLockType(transaction, name);
    }

    /**
     * Gets the type of lock that the transaction has at this level, either
     * implicitly (e.g. explicit S lock at higher level implies S lock at this
     * level) or explicitly. Returns NL if there is no explicit nor implicit
     * lock.
     */
    public LockType getEffectiveLockType(TransactionContext transaction) {
        if (transaction == null) return LockType.NL;
        LockType cur = getExplicitLockType(transaction);
        if (parent != null && cur == LockType.NL) {
            cur = parent.getEffectiveLockType(transaction);
            if (cur == LockType.S || cur == LockType.X || cur == LockType.SIX) {
                return cur;
            } else {
                return LockType.NL;
            }
        }
        return cur;
    }

    /**
     * Helper method to see if the transaction holds a SIX lock at an ancestor
     * of this context
     * @param transaction the transaction
     * @return true if holds a SIX at an ancestor, false if not
     */
    private boolean hasSIXAncestor(TransactionContext transaction) {
        // TODO(proj4_part2): implement
        if (parent == null) {
            return false;
        } else {
            if (parent.lockman.getLockType(transaction, name).equals(LockType.SIX)) {
                return true;
            } else {
                return parent.hasSIXAncestor(transaction);
            }
        }

    }

    /**
     * Helper method to get a list of resourceNames of all locks that are S or
     * IS and are descendants of current context for the given transaction.
     * @param transaction the given transaction
     * @return a list of ResourceNames of descendants which the transaction
     * holds an S or IS lock.
     */
    private List<ResourceName> sisDescendants(TransactionContext transaction) {
        // TODO(proj4_part2): implement
        List<ResourceName> resourceNameList = new ArrayList<>();
        List<Lock> locks = lockman.getLocks(transaction);
        for (Lock lock: locks) {
            if (lock.name.isDescendantOf(this.name)) {
                if (lock.lockType.equals(LockType.S) || lock.lockType.equals(LockType.IS)) {
                    resourceNameList.add(lock.name);
                }
            }
        }
        return resourceNameList;
    }

    /**
     * Disables locking descendants. This causes all new child contexts of this
     * context to be readonly. This is used for indices and temporary tables
     * (where we disallow finer-grain locks), the former due to complexity
     * locking B+ trees, and the latter due to the fact that temporary tables
     * are only accessible to one transaction, so finer-grain locks make no
     * sense.
     */
    public void disableChildLocks() {
        this.childLocksDisabled = true;
    }

    /**
     * Gets the parent context.
     */
    public LockContext parentContext() {
        return parent;
    }

    /**
     * Gets the context for the child with name `name` and readable name
     * `readable`
     */
    public synchronized LockContext childContext(String name) {
        LockContext temp = new LockContext(lockman, this, name,
                this.childLocksDisabled || this.readonly);
        LockContext child = this.children.putIfAbsent(name, temp);
        if (child == null) child = temp;
        return child;
    }

    /**
     * Gets the context for the child with name `name`.
     */
    public synchronized LockContext childContext(long name) {
        return childContext(Long.toString(name));
    }

    /**
     * Gets the number of locks held on children a single transaction.
     */
    public int getNumChildren(TransactionContext transaction) {
        return numChildLocks.getOrDefault(transaction.getTransNum(), 0);
    }

    @Override
    public String toString() {
        return "LockContext(" + name.toString() + ")";
    }
}

