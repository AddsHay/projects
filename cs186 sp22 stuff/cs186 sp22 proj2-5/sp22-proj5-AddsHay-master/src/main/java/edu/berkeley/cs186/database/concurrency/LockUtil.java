package edu.berkeley.cs186.database.concurrency;

import edu.berkeley.cs186.database.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * LockUtil is a declarative layer which simplifies multigranularity lock
 * acquisition for the user (you, in the last task of Part 2). Generally
 * speaking, you should use LockUtil for lock acquisition instead of calling
 * LockContext methods directly.
 */
public class LockUtil {
    /**
     * Ensure that the current transaction can perform actions requiring
     * `requestType` on `lockContext`.
     *
     * `requestType` is guaranteed to be one of: S, X, NL.
     *
     * This method should promote/escalate/acquire as needed, but should only
     * grant the least permissive set of locks needed. We recommend that you
     * think about what to do in each of the following cases:
     * - The current lock type can effectively substitute the requested type
     * - The current lock type is IX and the requested lock is S
     * - The current lock type is an intent lock
     * - None of the above: In this case, consider what values the explicit
     *   lock type can be, and think about how ancestor locks will need to be
     *   acquired or changed.
     *
     * You may find it useful to create a helper method that ensures you have
     * the appropriate locks on all ancestors.
     */
    public static void ensureSufficientLockHeld(LockContext lockContext, LockType requestType) {
        // requestType must be S, X, or NL
        assert (requestType == LockType.S || requestType == LockType.X || requestType == LockType.NL);

        // Do nothing if the transaction or lockContext is null
        TransactionContext transaction = TransactionContext.getTransaction();
        if (transaction == null || lockContext == null) return;

        // You may find these variables useful
        LockContext parentContext = lockContext.parentContext();
        LockType effectiveLockType = lockContext.getEffectiveLockType(transaction);
        LockType explicitLockType = lockContext.getExplicitLockType(transaction);
        if (requestType.equals(LockType.NL) || requestType.equals(effectiveLockType)) {
            return;
        }
        List<LockContext> ancestors = getAncestor(lockContext);
        if (LockType.substitutable(requestType, effectiveLockType)) {
            if (explicitLockType.equals(LockType.NL)) {
                changeAncestor(requestType, ancestors, transaction);
                lockContext.acquire(transaction, requestType);
            } else {
                changeAncestor(requestType, ancestors, transaction);
                lockContext.promote(transaction, requestType);
            }
        } else if (effectiveLockType.equals(LockType.IX) && requestType.equals(LockType.S)) {
            lockContext.promote(transaction, LockType.SIX);

        } else if (effectiveLockType.equals(LockType.IS) || effectiveLockType.equals(LockType.IX) || effectiveLockType.equals(LockType.SIX)) {
            changeAncestor(lockContext.getExplicitLockType(transaction), ancestors, transaction);
            lockContext.escalate(transaction);
            if (effectiveLockType.equals(LockType.IS) && requestType.equals(LockType.X)) {
                changeAncestor(LockType.X, ancestors, transaction);
                lockContext.promote(transaction, LockType.X);
            }
        } else {
            if (explicitLockType.equals(LockType.NL)) {
                return;
            }
            changeAncestor(explicitLockType, ancestors, transaction);
        }


        // TODO(proj4_part2): implement

    }

    // TODO(proj4_part2) add any helper methods you want
    public static List<LockContext> getAncestor(LockContext lockContext) {
        List<LockContext> temp = new ArrayList<>();
        List<LockContext> ancestors = new ArrayList<>();
        LockContext parentLockContext = lockContext.parentContext();
        while (parentLockContext != null) {
            temp.add(parentLockContext);
            parentLockContext = parentLockContext.parentContext();
        }
        for (int i = temp.size() - 1; i >= 0; i--) {
            ancestors.add(temp.get(i));
        }
        return ancestors;
    }
    public static void changeAncestor(LockType currLockType, List<LockContext> ancestors, TransactionContext transaction) {
        for (int i = 0; i < ancestors.size(); i++) {
            LockContext parentLockContext = ancestors.get(i);
            LockType parentLockType = parentLockContext.getEffectiveLockType(transaction);
            if (parentLockType.equals(LockType.NL)) {
                if (currLockType.equals(LockType.S)) {
                    parentLockContext.acquire(transaction, LockType.IS);
                } else if (currLockType.equals(LockType.X)) {
                    parentLockContext.acquire(transaction, LockType.IX);
                }
            }
            if (currLockType.equals(LockType.X)) {
                if (parentLockType.equals(LockType.S)) {
                    parentLockContext.promote(transaction, LockType.X);
                    return;
                } else if (parentLockType.equals(LockType.IS)) {
                    parentLockContext.promote(transaction, LockType.IX);
                }
            } else if (currLockType.equals(LockType.SIX)) {
                if (!parentLockType.equals(LockType.X)) {
                    parentLockContext.escalate(transaction);
                }

            }
        }
    }

}
