import numpy as np
import scipy
import scipy.optimize
import sys
import socket
import select

PRECISION = 3
MAX_FRAC = pow(10, PRECISION)

def parse_gambles(lines):
    """ Extract the gambles from a list of lines. """

    assert(lines[0] == 'gamble')
    gambles = []
    for line in lines[1:]:
        gambles.append(eval(line))
    return gambles

def parse_links(lines):
    """ Extract the links from a list of lines. """

    assert(lines[0] == 'link')
    links = []
    for line in lines[1:]:
        links.append(eval(line))
    return links

def parse_returns(str_returns):
    """ Extract list of returns from string. """

    return [eval(s) for s in str_returns.split(',')]

def compute_expected_return(gamble):
    """ Compute gamble expected return. """
    return ((gamble[2] * gamble[3]) +
            (gamble[4] * gamble[5]) +
            (gamble[6] * gamble[7]))

def qp_markowitz(R, u_T, D_T, qp_lambda, X0=None):
    """ Compute Quadratic Markowitz. """

    # Objective function.
    F_t = lambda t: lambda X: (
                    -(qp_lambda * np.dot(u_T[t], X)) +
                     ((1./(t+1)) * np.dot(np.dot(D_T[:t+1], X),
                                          np.dot(D_T[:t+1], X))))
    F_t_prime = lambda t: lambda X: (
                          -(qp_lambda * u_T[t]) +
                           ((1./(t+1)) * 2 * np.dot(D_T[:t+1].T,
                                                    np.dot(D_T[:t+1], X))))
    # The Lagrangian.
    L_t = lambda t: lambda X, a, B, C: (
                    F_t(t)(X) +
                    (a * (sum(X) - 1)**2) -
                    (np.dot(B, X)) -
                    (np.dot(C, np.ones(len(X)) - X)))
    L_t_prime = lambda t: lambda X, a, B, C: (
                          F_t_prime(t)(X) +
                          (a * 2 * np.ones(len(X)) * (sum(X) - 1)) -
                          B +
                          C)
    # Initial function arguments
    #if X0 is None:
    # reissb -- 20111123 -- Using previous X causes bad results.
    X0 = np.ones(R.shape[1]) / float(R.shape[1])
    a0 = 100.
    B0 = np.ones(len(R[0])) * 100.
    C0 = np.ones(len(R[0])) * 100.
    args = (a0, B0, C0)
    # Bounds
    bounds = [(0., 1.) for i in range(len(R[0]))]

    T = R.shape[0] - 1
#    print "T", T
#    print "R", R
#    print "u_T", u_T
#    print "D_T", D_T
    results = scipy.optimize.fmin_tnc(L_t(T), x0=X0,
                                      fprime=L_t_prime(T),
                                      args=args,
                                      bounds=bounds,
                                      messages=scipy.optimize.tnc.MSG_NONE)
    return results[0]

def remove_blank_lines(lines):
    """ Filter out blank lines. """

    return filter(lambda l: len(l.strip()) > 0, lines)

def socket_recv_all(s, timeout=None):
    """ Receive all queued data on the given socket. """

    str_read = ""
    r_ready, w_ready, x_ready = select.select([s.fileno()], [], [], timeout)
    while True:
        str_more = ""
        if len(r_ready) > 0:
            str_more = s.recv(4096)
        if len(str_more) > 0:
            str_read = str_read + str_more
        else:
            return str_read
        r_ready, w_ready, x_ready = select.select([s.fileno()], [], [], .005)

def serialize_list(l):
    """ Serialize a list in the game format 'l_0,...,l_n-1'. """

    s = "{0:0.3f}".format(l[0])
    return reduce(lambda s, e: "{0}, {1:0.3f}".format(s, e), l[1:], s)

def alloc_normalize(alloc_denorm):
    """
    Normalize and scale allocations. Play on the safe side by making the norm
    of the allocation vector slighly smaller than 1 to avoid being accused of
    cheating by the server.
    """

    assert((alloc_denorm >= 0).all())
    slightly_gt_one = 1. + (1. / MAX_FRAC)
    alloc_norm = alloc_denorm / (sum(alloc_denorm) * slightly_gt_one)
    alloc_trunc = (alloc_norm * MAX_FRAC).astype(int)
    return alloc_trunc / float(MAX_FRAC)

def play_double_wealth_game(command_args, s, gambles, links):
    """
    Play a game where the objective is to double your wealth at each
    independent round.
    """

    QP_LAMBDA = 0.25
    np.set_printoptions(precision=PRECISION+1, suppress=True)

    # Initialze R and compute Quadratic Markowitz.
    R = np.array([[1.] + [compute_expected_return(g) for g in gambles]])
    u_T = np.array([np.average(R[:t], axis=0) for t in range(1, R.shape[0]+1)])
    D_T = R - u_T
    alloc_denorm = qp_markowitz(R, u_T, D_T, QP_LAMBDA)
    alloc = alloc_normalize(alloc_denorm)
    print "Allocation:\n{0}".format(alloc)
    # Don't send allocation for cash holdings.
    str_alloc = serialize_list(alloc[1:])
    s.send(str_alloc + '\n')

    # Loop server iterations.
    while True:
        str_response = socket_recv_all(s).strip()
#        print "str_response", str_response
        response_lines = str_response.split('\n')
        response_lines = remove_blank_lines(response_lines)
        returns = parse_returns(response_lines[0].strip())
        R = np.vstack((R, [1.] + returns))
        u_T = np.vstack((u_T, np.average(R, axis=0)))
        D_T = np.vstack((D_T, u_T[-1] - R[-1]))
#        print response_lines[1]
        amounts = [eval(a) for a in response_lines[1].strip().split(',')]
#        print "Amounts:", amounts
        print "Returns:\n{0}".format(np.array(returns))
        wealth = np.sum(amounts)
        print "Wealth: {0}".format(wealth)
        assert(len(response_lines) == 3)
        if response_lines[2].find('END') == 0:
            print "Game over."
            print response_lines[2]
            break;
        else:
            print response_lines[2]
            # Compute Quadratic Markowitz.
            alloc_denorm = qp_markowitz(R, u_T, D_T, QP_LAMBDA, X0=alloc)
            alloc = alloc_normalize(alloc_denorm)
            print "Allocation:\n{0}".format(alloc)
            # Don't send allocation for cash holdings.
            str_alloc = serialize_list(alloc[1:])
            s.send(str_alloc + '\n')

def play_cumulative_wealth_game(command_args, s, gambles, links):
    """
    Play a long-term investment game.
    To optimize for getting rich, slect a higher QP_LAMBDA around 8. To
    optimize the Sharpe Ratio, select a small QP_LAMBDA less than 1.
    """

    QP_LAMBDA = 0.25
    np.set_printoptions(precision=PRECISION+1, suppress=True)

    # Initialze R and compute Quadratic Markowitz.
    wealth = 1.
    R = np.array([[1.] + [compute_expected_return(g) for g in gambles]])
    u_T = np.array([np.average(R[:t], axis=0) for t in range(1, R.shape[0]+1)])
    D_T = R - u_T
    risk_free_ret = np.array([np.average(R)])
    actual_return = np.array([])
    alloc_denorm = qp_markowitz(R, u_T, D_T, QP_LAMBDA)
    alloc_norm = alloc_normalize(alloc_denorm)
    alloc = alloc_norm * wealth
    print "Allocation:\n{0}".format(alloc_norm)
    # Don't send allocation for cash holdings.
    str_alloc = serialize_list(alloc[1:])
    s.send(str_alloc + '\n')

    # Loop server iterations.
    while True:
        str_response = socket_recv_all(s).strip()
#        print "str_response", str_response
        response_lines = str_response.split('\n')
        response_lines = remove_blank_lines(response_lines)
        returns = parse_returns(response_lines[0].strip())
        R = np.vstack((R, [1.] + returns))
        u_T = np.vstack((u_T, np.average(R, axis=0)))
        D_T = np.vstack((D_T, u_T[-1] - R[-1]))
#        print response_lines[1]
        amounts = [eval(a) for a in response_lines[1].strip().split(',')]
#        print response_lines[2]
        str_positions = response_lines[2].strip()
        positions = dict([(lambda x: (x[0], float(x[1])))(p.split(':'))
                          for p in str_positions.split(',')])
        print "Positions: {0}".format(positions)
#        print "Amounts:", amounts
        print "Returns:\n{0}".format(np.array(returns))
        wealth_now = np.sum(amounts)
        round_return = wealth_now / wealth
        actual_return = np.hstack((actual_return, [round_return]))
        sharpe_ratio = np.average(actual_return - risk_free_ret) / \
                       np.std(actual_return - risk_free_ret)
        wealth = wealth_now
        print "Wealth: {0}".format(wealth)
        print "Sharpe Ratio: {0}".format(sharpe_ratio)
        assert(len(response_lines) == 4)
        if response_lines[3].find('END') == 0:
            print "Game over."
            print response_lines[3]
            break;
        else:
            print response_lines[3]
            # Compute Quadratic Markowitz.
            alloc_denorm = qp_markowitz(R, u_T, D_T, QP_LAMBDA, X0=alloc_norm)
            alloc_norm = alloc_normalize(alloc_denorm)
            alloc = alloc_norm * wealth
            print "Allocation:\n{0}".format(alloc_norm)
            # Don't send allocation for cash holdings.
            str_alloc = serialize_list(alloc[1:])
            s.send(str_alloc + '\n')

def main(argv):
    """ Quadratic Markowitz main method. """

    # Collect positional parameters.
    if len(argv) is not 7:
        print "Usage: {0} HOST PORT MODE GAMBLES CLASSES NAME".format(argv[0])
        return -1
    else:
        command_args = { 'hostname': argv[1],
                         'port': int(argv[2]),
                         'mode': int(argv[3]),
                         'gambles': int(argv[4]),
                         'classes': int(argv[5]),
                         'name': argv[6] }

    # Connect. Let exceptions happen so that we can see trace.
    print "Connecting to server({0}, {1}).".format(command_args['hostname'],
                                                   command_args['port'])
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((command_args['hostname'], command_args['port']))

    # Send player name.
    s.send(command_args['name'] + '\n')

    # Read tables from server.
    str_server_data = socket_recv_all(s).strip()
    lines = str_server_data.split('\n')
    lines = remove_blank_lines(lines)
    link_idx = lines.index('link')
    try:
        give_alloc_idx = lines.index('Give allocation:')
    except:
        give_alloc_idx = -1
    gambles = parse_gambles(lines[:link_idx])
    links = parse_links(lines[link_idx:give_alloc_idx])
    print "num gambles: {0}".format(len(gambles))
    print "num links: {0}".format(len(links))

    if give_alloc_idx < 0:
        str_command = socket_recv_all(s)
    else:
        str_command = lines[give_alloc_idx]
    print str_command

    # Play the game.
    if command_args['mode'] == 1:
        play_double_wealth_game(command_args, s, gambles, links)
    else:
        play_cumulative_wealth_game(command_args, s, gambles, links)

# Main method idiom.
if __name__ == "__main__":
    main(sys.argv)

